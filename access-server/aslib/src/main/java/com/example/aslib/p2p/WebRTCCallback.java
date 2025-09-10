package com.example.aslib.p2p;

public interface WebRTCCallback {
    /**
     * 连接成功回调
     * @param clientId 客户端ID
     */
    void onConnected(String clientId);

    /**
     * 用户加入房间回调
     * @param userId 用户ID
     */
    void onUserJoined(String userId);

    /**
     * 用户离开房间回调
     * @param userId 用户ID
     */
    void onUserLeft(String userId);

    /**
     * 接收到文本消息
     * @param senderId 发送者ID
     * @param message 消息内容
     * @param isP2P 是否为P2P直连消息
     */
    void onMessageReceived(String senderId, String message, boolean isP2P);

    /**
     * 接收到二进制数据
     * @param senderId 发送者ID
     * @param data 二进制数据
     * @param isP2P 是否为P2P直连数据
     */
    void onBinaryDataReceived(String senderId, byte[] data, boolean isP2P);

    /**
     * 连接状态变化
     * @param state 新的连接状态
     */
    void onConnectionStateChanged(String state);

    /**
     * 错误回调
     * @param error 错误信息
     */
    void onError(String error);
}