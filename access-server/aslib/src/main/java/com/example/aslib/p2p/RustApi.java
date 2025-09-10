package com.example.aslib.p2p;
public class RustApi {
    // 加载本地库
    static {
//        System.loadLibrary("rust_jni"); // 对应 librust_jni.so
    }

    /**
     * 创建WebRTC客户端
     * @param roomId 房间ID
     * @param serverUrl 信令服务器地址
     * @param debug 是否开启调试模式
     * @param callback 回调接口
     * @return 客户端ID，用于后续操作
     */
    public static native String createWebRTCClient(
            String roomId,
            String serverUrl,
            boolean debug,
            WebRTCCallback callback
    );

    /**
     * 连接到服务器
     * @param clientId 客户端ID
     * @return 是否连接成功
     */
    public static native boolean connect(String clientId);

    /**
     * 发送文本消息
     * @param clientId 客户端ID
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    public static native boolean sendMessage(String clientId, String message);

    /**
     * 发送二进制数据
     * @param clientId 客户端ID
     * @param data 要发送的二进制数据
     * @return 是否发送成功
     */
    public static native boolean sendBinaryData(String clientId, byte[] data);

    /**
     * 关闭连接
     * @param clientId 客户端ID
     * @return 是否关闭成功
     */
    public static native boolean close(String clientId);

}