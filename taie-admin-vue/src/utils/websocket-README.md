# WebSocket 通讯库重构说明

## 概述

基于 `WebSocketHandler2.java` 的协议结构，将原本 `detail.vue` 中的 WebSocket 通讯代码重构为独立的可复用库。

## 协议结构

### 消息类型 (前4字节)
- `MSG_TYPE_ROOM_NOTIFICATION = 0x00000001` - 房间通知消息
- `MSG_TYPE_CLIENT_MESSAGE = 0x00000002` - 客户端消息  
- `MSG_TYPE_HEARTBEAT = 0x00000003` - 心跳消息

### 房间通知子类型
- `ROOM_EVENT_CLIENT_JOINED = 0x01` - 客户端加入
- `ROOM_EVENT_CLIENT_LEFT = 0x02` - 客户端离开
- `ROOM_EVENT_CLIENT_ERROR = 0x03` - 客户端错误
- `ROOM_EVENT_ROOM_MEMBER_COUNT = 0x04` - 房间成员数量

## 核心功能

### 1. WebSocketClient 类

位置：`src/utils/websocket-client.ts`

主要功能：
- ✅ 自动心跳机制（默认25秒间隔）
- ✅ 自动重连机制（默认最多重连5次）
- ✅ 房间管理（基于房间ID）
- ✅ 消息类型自动处理
- ✅ 完整的事件回调系统

### 2. 配置选项

```typescript
interface WebSocketConfig {
  url: string;                    // WebSocket 服务器地址
  roomId: string;                // 房间ID（通常使用设备ID）
  heartbeatInterval?: number;     // 心跳间隔，默认25秒
  reconnectInterval?: number;     // 重连间隔，默认3秒
  maxReconnectAttempts?: number;  // 最大重连次数，默认5次
}
```

### 3. 事件处理器

```typescript
interface MessageHandler {
  onMessage?: (data: ArrayBuffer) => void;           // 业务消息
  onRoomNotification?: (notification) => void;      // 房间通知
  onHeartbeat?: () => void;                          // 心跳响应
  onConnect?: () => void;                            // 连接成功
  onDisconnect?: () => void;                         // 连接断开
  onError?: (error: Event) => void;                  // 连接错误
  onReconnecting?: (attempt: number) => void;        // 重连中
  onReconnectFailed?: () => void;                    // 重连失败
}
```

## 使用方法

### 基本用法

```typescript
import { WebSocketClient } from '@/utils/websocket-client';
import { encodeWsMessage, MessageType } from '@/utils/message';

// 1. 创建客户端
const wsClient = new WebSocketClient({
  url: window.wsUrl,
  roomId: deviceId,
  heartbeatInterval: 25000,
  reconnectInterval: 3000,
  maxReconnectAttempts: 5
});

// 2. 设置事件处理器
wsClient.setHandlers({
  onConnect: () => {
    console.log('连接成功');
    // 发送初始化消息
    const msg = encodeWsMessage(MessageType.monitor_online, { deviceId });
    wsClient.sendMessage(msg);
  },
  onMessage: (data) => {
    // 处理业务消息
    const { type, body } = decodeWsMessage(new Uint8Array(data));
    // ... 处理不同类型的消息
  },
  onDisconnect: () => {
    console.log('连接断开');
  }
});

// 3. 连接
await wsClient.connect();

// 4. 发送消息
const touchMsg = encodeWsMessage(MessageType.touch_req, { 
  deviceId, x: 100, y: 200, hold: true 
});
wsClient.sendMessage(touchMsg);

// 5. 断开连接
wsClient.disconnect();
```

## 重构内容

### 修改的文件

1. **新增：`src/utils/websocket-client.ts`**
   - 核心 WebSocket 通讯库
   - 实现完整的协议支持
   - 提供丰富的配置选项和事件回调

2. **修改：`src/views/device/detail.vue`**
   - 替换原生 WebSocket 为 WebSocketClient
   - 保持原有业务逻辑不变
   - 增强连接稳定性和错误处理

3. **新增：`src/utils/websocket-example.ts`**
   - 使用示例和最佳实践
   - 展示各种消息类型的发送方法

### 主要改进

1. **协议兼容性**
   - 完全兼容 WebSocketHandler2.java 的协议结构
   - 正确处理消息类型标识和房间通知

2. **连接稳定性**
   - 自动心跳检测，防止连接超时
   - 智能重连机制，网络波动时自动恢复
   - 完善的错误处理和状态管理

3. **代码可维护性**
   - 单一职责原则，通讯逻辑与业务逻辑分离
   - 类型安全，完整的 TypeScript 支持
   - 易于测试和调试

4. **使用便利性**
   - 简单的 API 设计
   - 丰富的配置选项
   - 详细的事件回调

## 兼容性说明

- ✅ 完全兼容现有的消息格式（encodeWsMessage/decodeWsMessage）
- ✅ 保持原有业务逻辑不变
- ✅ 支持 ArrayBuffer 和 Uint8Array 消息格式
- ✅ 向后兼容，可以逐步迁移其他页面

## 后续扩展

这个 WebSocket 库可以轻松扩展到其他需要 WebSocket 通讯的页面：

```typescript
// 其他页面可以直接使用
import { WebSocketClient } from '@/utils/websocket-client';

// 创建专用的业务封装
class DeviceWebSocketManager {
  private wsClient: WebSocketClient;
  
  constructor(deviceId: string) {
    this.wsClient = new WebSocketClient({
      url: window.wsUrl,
      roomId: deviceId
    });
    this.setupHandlers();
  }
  
  private setupHandlers() {
    // 业务相关的消息处理逻辑
  }
}
```
