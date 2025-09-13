/**
 * WebSocket 客户端使用示例
 * 展示如何使用重构后的 WebSocket 通讯库
 */

import { WebSocketClient, ROOM_EVENT_CLIENT_JOINED, ROOM_EVENT_CLIENT_LEFT } from './websocket-client';
import { encodeWsMessage, decodeWsMessage, MessageType } from './message';

// 使用示例
export function createWebSocketExample(deviceId: string) {
  // 创建 WebSocket 客户端
  const wsClient = new WebSocketClient({
    url: 'ws://localhost:8080/as/ws', // 替换为实际的 WebSocket 服务器地址
    roomId: deviceId,
    heartbeatInterval: 25000, // 25秒心跳
    reconnectInterval: 3000,  // 3秒重连间隔
    maxReconnectAttempts: 5   // 最大重连5次
  });

  // 设置消息处理器
  wsClient.setHandlers({
    onConnect: () => {
      console.log('✅ WebSocket 连接成功');
      
      // 连接成功后可以发送初始化消息
      const initMsg = encodeWsMessage(MessageType.monitor_online, { deviceId });
      wsClient.sendMessage(initMsg);
    },

    onMessage: (data: ArrayBuffer) => {
      // 处理业务消息
      const { type, body } = decodeWsMessage(new Uint8Array(data));
      console.log('📨 收到消息:', type, body);
      
      switch (type) {
        case MessageType.screen_info:
          console.log('🖥️ 屏幕信息更新:', body);
          break;
        case MessageType.notify:
          console.log('🔔 收到通知:', body);
          break;
        default:
          console.log('❓ 未知消息类型:', type);
      }
    },

    onRoomNotification: (notification) => {
      console.log('🏠 房间通知:', notification);
      
      switch (notification.eventType) {
        case ROOM_EVENT_CLIENT_JOINED:
          console.log(`👋 客户端 ${notification.sessionId} 加入房间`);
          break;
        case ROOM_EVENT_CLIENT_LEFT:
          console.log(`👋 客户端 ${notification.sessionId} 离开房间`);
          break;
        default:
          console.log('❓ 未知房间事件:', notification.eventType);
      }
    },

    onHeartbeat: () => {
      console.log('💓 心跳响应');
    },

    onDisconnect: () => {
      console.log('❌ WebSocket 连接断开');
    },

    onError: (error) => {
      console.error('💥 WebSocket 错误:', error);
    },

    onReconnecting: (attempt) => {
      console.log(`🔄 正在进行第 ${attempt} 次重连...`);
    },

    onReconnectFailed: () => {
      console.error('💀 重连失败，请检查网络连接');
    }
  });

  // 连接到服务器
  wsClient.connect().catch(error => {
    console.error('连接失败:', error);
  });

  // 返回客户端实例，以便外部调用其他方法
  return {
    wsClient,
    
    // 发送点击消息
    sendClick: (x: number, y: number) => {
      const clickMsg = encodeWsMessage(MessageType.touch_req, { 
        deviceId, 
        x, 
        y, 
        hold: true 
      });
      wsClient.sendMessage(clickMsg);
    },

    // 发送滑动消息
    sendSlide: (points: Array<{x: number, y: number, delay: number}>) => {
      const slideMsg = encodeWsMessage(MessageType.slide_req, { 
        deviceId, 
        points, 
        segmentSize: 10 
      });
      wsClient.sendMessage(slideMsg);
    },

    // 请求屏幕信息
    requestScreen: () => {
      const screenMsg = encodeWsMessage(MessageType.screen_req, { deviceId });
      wsClient.sendMessage(screenMsg);
    },

    // 请求房间成员数量
    requestRoomMemberCount: () => {
      wsClient.requestRoomMemberCount();
    },

    // 断开连接
    disconnect: () => {
      wsClient.disconnect();
    },

    // 检查连接状态
    isConnected: () => {
      return wsClient.isWebSocketConnected();
    }
  };
}
