/**
 * WebSocket 客户端库
 * 基于 WebSocketHandler2.java 的协议结构
 */

export interface WebSocketConfig {
  url: string;
  roomId: string;
  heartbeatInterval?: number; // 心跳间隔，默认25秒
  reconnectInterval?: number; // 重连间隔，默认3秒
  maxReconnectAttempts?: number; // 最大重连次数，默认5次
}

export interface RoomNotification {
  eventType: number;
  value: string;
}

export interface MessageHandler {
  onMessage?: (data: ArrayBuffer) => void;
  onRoomNotification?: (notification: RoomNotification) => void;
  onHeartbeat?: () => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
  onReconnecting?: (attempt: number) => void;
  onReconnectFailed?: () => void;
}

// 消息类型常量
const MSG_TYPE_ROOM_NOTIFICATION = 0x00000001;
const MSG_TYPE_CLIENT_MESSAGE = 0x00000002;
const MSG_TYPE_HEARTBEAT = 0x00000003;

// 房间通知子类型
export const ROOM_EVENT_CLIENT_JOINED = 0x01;
export const ROOM_EVENT_CLIENT_LEFT = 0x02;
export const ROOM_EVENT_CLIENT_ERROR = 0x03;
export const ROOM_EVENT_ROOM_MEMBER_COUNT = 0x04;

export class WebSocketClient {
  private ws: WebSocket | null = null;
  private config: Required<WebSocketConfig>;
  private handlers: MessageHandler = {};
  private heartbeatTimer: number | null = null;
  private reconnectTimer: number | null = null;
  private reconnectAttempts = 0;
  private isConnected = false;
  private isDestroyed = false;
  private isReconnecting = false; // 添加重连状态标记
  private reconnectFailedTriggered = false; // 添加标记防止重复触发onReconnectFailed

  constructor(config: WebSocketConfig) {
    this.config = {
      heartbeatInterval: 25000, // 25秒，比服务端30秒检测稍快
      reconnectInterval: 3000,
      maxReconnectAttempts: 5,
      ...config
    };
  }

  /**
   * 连接到 WebSocket 服务器
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        const url = `${this.config.url}?roomId=${encodeURIComponent(this.config.roomId)}`;
        this.ws = new WebSocket(url);
        this.ws.binaryType = 'arraybuffer';

        this.ws.onopen = () => {
          console.log('WebSocket 连接成功');
          this.isConnected = true;
          this.isReconnecting = false; // 重置重连状态
          this.reconnectAttempts = 0;
          this.reconnectFailedTriggered = false; // 重置失败标记
          this.startHeartbeat();
          this.handlers.onConnect?.();
          resolve();
        };

        this.ws.onmessage = (event) => {
          this.handleMessage(event.data);
        };

        this.ws.onclose = (event) => {
          console.log('WebSocket 连接关闭:', event.code, event.reason);
          this.isConnected = false;
          this.stopHeartbeat();
          this.handlers.onDisconnect?.();
          
          if (!this.isDestroyed && event.code !== 1000 && !this.isReconnecting) {
            // 非正常关闭且不在重连中，尝试重连
            this.scheduleReconnect();
          }
        };

        this.ws.onerror = (error) => {
          console.error('WebSocket 连接错误:', error);
          this.handlers.onError?.(error);
          reject(error);
        };

      } catch (error) {
        reject(error);
      }
    });
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isDestroyed = true;
    this.isReconnecting = false; // 重置重连状态
    this.stopHeartbeat();
    this.stopReconnect();
    
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.close(1000, 'Client disconnect');
    }
    this.ws = null;
    this.isConnected = false;
  }

  /**
   * 发送原始消息（会自动添加客户端消息类型标识）
   */
  sendMessage(data: ArrayBuffer | Uint8Array): void {
    if (!this.isConnected || !this.ws) {
      console.warn('WebSocket 未连接，无法发送消息');
      return;
    }

    // 将输入转换为 Uint8Array
    const inputData = data instanceof ArrayBuffer ? new Uint8Array(data) : data;

    // 构建客户端消息：4字节消息类型 + 原始数据
    const buffer = new ArrayBuffer(4 + inputData.byteLength);
    const view = new DataView(buffer);
    
    view.setUint32(0, MSG_TYPE_CLIENT_MESSAGE, false); // 大端序
    new Uint8Array(buffer, 4).set(inputData);
    
    this.ws.send(buffer);
  }

  /**
   * 发送心跳消息
   */
  private sendHeartbeat(): void {
    if (!this.isConnected || !this.ws) {
      return;
    }

    const buffer = new ArrayBuffer(4);
    const view = new DataView(buffer);
    view.setUint32(0, MSG_TYPE_HEARTBEAT, false);
    
    this.ws.send(buffer);
  }

  /**
   * 请求房间成员数量
   */
  requestRoomMemberCount(): void {
    if (!this.isConnected || !this.ws) {
      console.warn('WebSocket 未连接，无法请求房间成员数量');
      return;
    }

    const buffer = new ArrayBuffer(5);
    const view = new DataView(buffer);
    
    view.setUint32(0, MSG_TYPE_ROOM_NOTIFICATION, false);
    view.setUint8(4, ROOM_EVENT_ROOM_MEMBER_COUNT);
    
    this.ws.send(buffer);
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(data: ArrayBuffer): void {
    if (data.byteLength < 4) {
      console.warn('接收到无效消息，长度不足');
      return;
    }

    const view = new DataView(data);
    const messageType = view.getUint32(0, false); // 大端序

    switch (messageType) {
      case MSG_TYPE_HEARTBEAT:
        // 心跳响应
        this.handlers.onHeartbeat?.();
        break;

      case MSG_TYPE_ROOM_NOTIFICATION:
        this.handleRoomNotification(data);
        break;

      case MSG_TYPE_CLIENT_MESSAGE:
        // 客户端消息，提取原始内容
        const originalData = data.slice(4);
        this.handlers.onMessage?.(originalData);
        break;

      default:
        console.warn('未知消息类型:', messageType);
        break;
    }
  }

  /**
   * 处理房间通知消息
   */
  private handleRoomNotification(data: ArrayBuffer): void {
    if (data.byteLength < 5) {
      console.warn('房间通知消息格式错误');
      return;
    }

    const view = new DataView(data);
    const eventType = view.getUint8(4);

    if (eventType === ROOM_EVENT_ROOM_MEMBER_COUNT) {
      // 房间成员数量响应
      if (data.byteLength >= 9) {
        const memberCount = view.getUint32(5, false);
        console.log('房间成员数量:', memberCount);
        this.handlers.onRoomNotification?.({
          eventType,
          value: memberCount.toString() // 这里用数量作为特殊值
        });
      }
    } else {
      // 其他房间事件：客户端加入/离开/错误
      if (data.byteLength >= 9) {
        const sessionIdLength = view.getUint32(5, false);
        if (data.byteLength >= 9 + sessionIdLength) {
          const sessionIdBytes = new Uint8Array(data, 9, sessionIdLength);
          const sessionId = new TextDecoder().decode(sessionIdBytes);
          
          this.handlers.onRoomNotification?.({
            eventType,
            value:sessionId
          });
        }
      }
    }
  }

  /**
   * 开始心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatTimer = window.setInterval(() => {
      this.sendHeartbeat();
    }, this.config.heartbeatInterval);
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    // 防止并发重连
    if (this.isReconnecting) {
      return;
    }

    // 检查是否应该停止重连
    if (this.isDestroyed || this.reconnectAttempts >= this.config.maxReconnectAttempts) {
      if (this.reconnectAttempts >= this.config.maxReconnectAttempts && !this.reconnectFailedTriggered) {
        console.error('达到最大重连次数，停止重连');
        this.reconnectFailedTriggered = true; // 设置标记防止重复触发
        this.handlers.onReconnectFailed?.();
      }
      return;
    }

    this.isReconnecting = true; // 设置重连状态
    this.reconnectAttempts++;
    console.log(`准备第 ${this.reconnectAttempts} 次重连...`);
    this.handlers.onReconnecting?.(this.reconnectAttempts);

    this.reconnectTimer = window.setTimeout(() => {
      this.connect().catch(() => {
        // 重连失败，重置状态后继续下一次重连
        this.isReconnecting = false;
        this.scheduleReconnect();
      });
    }, this.config.reconnectInterval);
  }

  /**
   * 停止重连
   */
  private stopReconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.isReconnecting = false; // 重置重连状态
  }

  /**
   * 设置消息处理器
   */
  setHandlers(handlers: MessageHandler): void {
    this.handlers = { ...this.handlers, ...handlers };
  }

  /**
   * 获取连接状态
   */
  isWebSocketConnected(): boolean {
    return this.isConnected;
  }

  /**
   * 获取房间ID
   */
  getRoomId(): string {
    return this.config.roomId;
  }
}
