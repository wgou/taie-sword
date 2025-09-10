# WebRTC 客户端模块使用说明

## 📋 概述

这是一个独立的 WebRTC 客户端 JavaScript 模块，专门用于与 Android 端进行 P2P 通信。Web 端等待 Android 端发起连接，支持文本消息和二进制数据传输。

## 🚀 快速开始

### 1. 引入模块

```html
<script src="webrtc-client.js"></script>
```

### 2. 创建客户端实例

```javascript
const webrtcClient = new WebRTCClient({
    serverUrl: 'ws://127.0.0.1:9001',  // 信令服务器地址
    debug: true,                        // 开启调试模式
    autoInitiateP2P: false,             // 是否自动发起P2P连接
    iceServers: [                      // STUN 服务器配置
        { urls: 'stun:stun.l.google.com:19302' }
    ],
    callbacks: {                       // 事件回调
        onConnected: () => console.log('已连接'),
        onMessageReceived: (sender, message, isP2P) => {
            console.log(`收到消息: ${message}`);
        }
        // ... 更多回调
    }
});
```

### 3. 连接到房间

```javascript
webrtcClient.connect('room123')
    .then(() => console.log('连接成功'))
    .catch(error => console.error('连接失败:', error));
```

### 4. 发送消息

```javascript
// 发送文本消息
webrtcClient.sendMessage('Hello World!');

// 发送二进制数据
const file = document.getElementById('fileInput').files[0];
webrtcClient.sendBinaryData(file);

// 手动发起P2P连接
await webrtcClient.manualInitiateP2P();

// 启用自动P2P连接模式
webrtcClient.setAutoInitiateP2P(true);
```

## 🔧 API 参考

### 构造函数选项

```javascript
new WebRTCClient({
    serverUrl: string,      // WebSocket 服务器地址，默认 'ws://127.0.0.1:9001'
    debug: boolean,         // 是否开启调试模式，默认 false
    autoInitiateP2P: boolean, // 是否自动发起P2P连接，默认 false
    iceServers: Array,      // STUN/TURN 服务器配置
    callbacks: Object       // 事件回调函数集合
})
```

### 回调函数

| 回调函数 | 参数 | 描述 |
|---------|------|------|
| `onConnected()` | 无 | WebSocket 连接建立 |
| `onDisconnected()` | 无 | 连接断开 |
| `onUserJoined(userId)` | userId: string | 用户加入房间 |
| `onUserLeft(userId)` | userId: string | 用户离开房间 |
| `onMessageReceived(sender, message, isP2P)` | sender: string, message: string, isP2P: boolean | 收到文本消息 |
| `onBinaryDataReceived(sender, data, isP2P)` | sender: string, data: ArrayBuffer/Blob, isP2P: boolean | 收到二进制数据 |
| `onConnectionStateChanged(state)` | state: string | 连接状态变化 |
| `onError(error)` | error: string | 发生错误 |
| `onDebug(message)` | message: string | 调试信息 |

### 主要方法

#### `connect(roomId)`
连接到指定房间
- **参数**: `roomId` (string) - 房间ID
- **返回**: Promise<boolean>

#### `sendMessage(message)`
发送文本消息
- **参数**: `message` (string) - 要发送的消息
- **返回**: boolean - 是否发送成功

#### `sendBinaryData(data)`
发送二进制数据
- **参数**: `data` (ArrayBuffer|Uint8Array|Blob) - 二进制数据
- **返回**: boolean - 是否发送成功

#### `disconnect()`
断开连接
- **参数**: 无
- **返回**: 无

#### `getConnectionState()`
获取当前连接状态
- **返回**: Object - 包含连接状态信息

#### `runDebugCheck()`
运行调试检查
- **返回**: Object - 调试信息

#### `getDebugLog(limit)`
获取调试日志
- **参数**: `limit` (number) - 日志条数限制，默认50
- **返回**: Array<string> - 调试日志数组

#### `manualInitiateP2P(targetClientId)`
手动发起P2P连接
- **参数**: `targetClientId` (string, 可选) - 目标客户端ID
- **返回**: Promise<boolean> - 是否成功发起连接

#### `setAutoInitiateP2P(enabled)`
设置自动发起P2P连接模式
- **参数**: `enabled` (boolean) - 是否启用自动发起模式
- **返回**: 无

#### `forceReconnectP2P()`
强制重新建立P2P连接
- **参数**: 无
- **返回**: Promise<boolean> - 是否成功发起重连

#### `canInitiateP2P()`
检查是否可以发起P2P连接
- **参数**: 无
- **返回**: boolean - 是否可以发起连接

#### `getP2PConnectionDetails()`
获取P2P连接状态详情
- **参数**: 无
- **返回**: Object - P2P连接状态详情

## 📝 使用示例

### 基础聊天应用

```javascript
// 创建客户端
const client = new WebRTCClient({
    serverUrl: 'ws://localhost:9001',
    debug: true,
    callbacks: {
        onConnected: () => {
            document.getElementById('status').textContent = '已连接';
        },
        onMessageReceived: (sender, message, isP2P) => {
            const type = isP2P ? 'P2P' : '转发';
            addMessage(`[${type}] ${sender}: ${message}`);
        },
        onConnectionStateChanged: (state) => {
            document.getElementById('mode').textContent = state;
        }
    }
});

// 连接按钮事件
document.getElementById('connectBtn').onclick = async () => {
    const roomId = document.getElementById('roomInput').value;
    try {
        await client.connect(roomId);
        console.log('连接成功');
    } catch (error) {
        console.error('连接失败:', error);
    }
};

// 发送按钮事件
document.getElementById('sendBtn').onclick = () => {
    const message = document.getElementById('messageInput').value;
    if (client.sendMessage(message)) {
        addMessage(`我: ${message}`);
        document.getElementById('messageInput').value = '';
    }
};
```

### 文件传输

```javascript
// 文件选择和发送
document.getElementById('fileInput').onchange = (e) => {
    const file = e.target.files[0];
    if (file && client.sendBinaryData(file)) {
        console.log(`发送文件: ${file.name}`);
    }
};

// 处理接收到的文件
const client = new WebRTCClient({
    callbacks: {
        onBinaryDataReceived: (sender, data, isP2P) => {
            if (data instanceof Blob) {
                // 创建下载链接
                const url = URL.createObjectURL(data);
                const a = document.createElement('a');
                a.href = url;
                a.download = `received_file_${Date.now()}`;
                a.click();
            }
        }
    }
});
```

### P2P连接控制

```javascript
// 创建客户端并配置P2P行为
const client = new WebRTCClient({
    autoInitiateP2P: false,  // 禁用自动发起，手动控制
    callbacks: {
        onUserJoined: (userId) => {
            console.log(`用户 ${userId} 加入了房间`);
            // 可以在这里决定是否发起P2P连接
        },
        onConnectionStateChanged: (state) => {
            console.log(`连接模式: ${state}`);
        }
    }
});

// 手动发起P2P连接
async function initiateP2P() {
    if (client.canInitiateP2P()) {
        const success = await client.manualInitiateP2P();
        if (success) {
            console.log('P2P连接发起成功');
        } else {
            console.log('P2P连接发起失败');
        }
    } else {
        console.log('当前无法发起P2P连接');
    }
}

// 启用自动P2P模式
function enableAutoP2P() {
    client.setAutoInitiateP2P(true);
    console.log('已启用自动P2P连接模式');
}

// 强制重新连接P2P
async function reconnectP2P() {
    const success = await client.forceReconnectP2P();
    console.log(success ? 'P2P重连成功' : 'P2P重连失败');
}

// 获取P2P连接详情
function showP2PDetails() {
    const details = client.getP2PConnectionDetails();
    console.log('P2P连接详情:', details);
    
    const state = client.getConnectionState();
    console.log('连接状态:', state);
}
```

## 🔍 调试

### 开启调试模式

```javascript
const client = new WebRTCClient({
    debug: true,  // 开启调试模式
    callbacks: {
        onDebug: (message) => {
            console.log('WebRTC Debug:', message);
        }
    }
});
```

### 获取调试信息

```javascript
// 运行调试检查
const debugInfo = client.runDebugCheck();
console.log('调试信息:', debugInfo);

// 获取调试日志
const logs = client.getDebugLog(20);  // 获取最近20条日志
console.log('调试日志:', logs);
```

## 🌟 特性

- ✅ **简单易用**: 一行代码引入，几行代码初始化
- ✅ **事件驱动**: 完整的回调函数支持
- ✅ **P2P优先**: 自动尝试P2P连接，失败时回退到服务器转发
- ✅ **灵活控制**: 支持自动和手动P2P连接发起模式
- ✅ **二进制支持**: 支持文件、图片等二进制数据传输
- ✅ **调试友好**: 内置调试功能，便于开发和问题排查
- ✅ **状态管理**: 完整的连接状态跟踪
- ✅ **错误处理**: 完善的错误处理和回调机制
- ✅ **连接重建**: 支持强制重新建立P2P连接

## 🔧 配置选项

### STUN 服务器配置

```javascript
const client = new WebRTCClient({
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        { urls: 'stun:stun1.l.google.com:19302' },
        { 
            urls: 'turn:your-turn-server.com:3478',
            username: 'username',
            credential: 'password'
        }
    ]
});
```

### 服务器地址配置

```javascript
const client = new WebRTCClient({
    serverUrl: 'wss://your-signaling-server.com:9001'  // 支持 WSS
});
```

## 📱 与 Android 端配合

1. **连接顺序**: Web端先连接，Android端后连接
2. **发起方**: Android端始终作为P2P连接发起方
3. **数据格式**: 兼容Android端的数据格式和协议

## 🚨 注意事项

1. **浏览器兼容性**: 需要支持WebRTC的现代浏览器
2. **HTTPS要求**: 在生产环境中需要HTTPS协议
3. **网络环境**: P2P连接可能受到NAT和防火墙影响
4. **文件大小**: 大文件传输建议分块处理

## 📄 许可证

MIT License
