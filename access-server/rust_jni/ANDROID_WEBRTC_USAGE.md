# Android WebRTC 使用指南

这个文档展示了如何在Android应用中使用优化后的WebRTC功能。

## JNI接口

### 1. 创建WebRTC客户端

```java
// 在Java中定义回调接口
public interface WebRTCCallback {
    void onConnected(String clientId);
    void onUserJoined(String userId);
    void onUserLeft(String userId);
    void onMessageReceived(String senderId, String message, boolean isP2P);
    void onConnectionStateChanged(String state);
    void onError(String error);
}

// 创建WebRTC客户端
String clientId = RustApi.createWebRTCClient(
    "room123",                    // 房间ID
    "ws://127.0.0.1:9001",       // 信令服务器URL
    true,                        // 调试模式
    new WebRTCCallback() {       // 回调接口
        @Override
        public void onConnected(String clientId) {
            Log.d("WebRTC", "已连接，客户端ID: " + clientId);
        }
        
        @Override
        public void onUserJoined(String userId) {
            Log.d("WebRTC", "用户加入: " + userId);
        }
        
        @Override
        public void onUserLeft(String userId) {
            Log.d("WebRTC", "用户离开: " + userId);
        }
        
        @Override
        public void onMessageReceived(String senderId, String message, boolean isP2P) {
            String type = isP2P ? "P2P" : "转发";
            Log.d("WebRTC", "收到" + type + "消息 [" + senderId + "]: " + message);
        }
        
        @Override
        public void onConnectionStateChanged(String state) {
            Log.d("WebRTC", "连接状态变化: " + state);
        }
        
        @Override
        public void onError(String error) {
            Log.e("WebRTC", "错误: " + error);
        }
    }
);
```

### 2. 连接到服务器

```java
boolean success = RustApi.connect(clientId);
if (success) {
    Log.d("WebRTC", "连接成功");
} else {
    Log.e("WebRTC", "连接失败");
}
```

### 3. 发送消息

```java
boolean success = RustApi.sendMessage(clientId, "Hello, World!");
if (success) {
    Log.d("WebRTC", "消息发送成功");
} else {
    Log.e("WebRTC", "消息发送失败");
}
```

### 4. 断开连接

```java
boolean success = RustApi.close(clientId);
if (success) {
    Log.d("WebRTC", "断开连接成功");
} else {
    Log.e("WebRTC", "断开连接失败");
}
```

## 功能特性

### 1. 自动P2P连接
- 当两个客户端加入同一房间时，系统会自动尝试建立P2P连接
- 使用客户端ID比较决定谁作为发起方，避免冲突
- P2P连接失败时自动回退到服务器转发模式

### 2. 消息传输
- 优先使用P2P直连发送消息（低延迟）
- P2P不可用时自动使用服务器转发
- 通过回调接口区分P2P和转发消息

### 3. 连接状态管理
- 实时监控连接状态变化
- 通过回调通知应用层状态变化
- 支持用户加入/离开通知

### 4. 错误处理
- 完整的错误处理机制
- 通过回调通知错误信息
- 自动重连和故障恢复

## 配置选项

WebRTC配置包含以下选项：
- `room_id`: 房间ID
- `server_url`: 信令服务器地址
- `debug`: 调试模式开关
- `stun_servers`: STUN服务器列表（自动配置Google STUN服务器）

## 注意事项

1. **线程安全**: 所有JNI接口都是线程安全的
2. **资源管理**: 使用完毕后记得调用`close()`释放资源
3. **网络权限**: 确保应用有网络访问权限
4. **回调线程**: 回调函数可能在后台线程中执行，UI更新需要切换到主线程

## 示例应用

```java
public class WebRTCManager {
    private String clientId;
    private WebRTCCallback callback;
    
    public WebRTCManager() {
        this.callback = new WebRTCCallbackImpl();
    }
    
    public void joinRoom(String roomId, String serverUrl) {
        clientId = RustApi.createWebRTCClient(roomId, serverUrl, true, callback);
        if (RustApi.connect(clientId)) {
            Log.d("WebRTC", "成功加入房间: " + roomId);
        }
    }
    
    public void sendMessage(String message) {
        if (clientId != null) {
            RustApi.sendMessage(clientId, message);
        }
    }
    
    public void leaveRoom() {
        if (clientId != null) {
            RustApi.close(clientId);
            clientId = null;
        }
    }
    
    private class WebRTCCallbackImpl implements WebRTCCallback {
        // 实现所有回调方法...
    }
}
```
