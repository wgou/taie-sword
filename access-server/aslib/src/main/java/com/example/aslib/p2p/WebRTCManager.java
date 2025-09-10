package com.example.aslib.p2p;
import android.graphics.Bitmap;
import android.util.Log;

import com.blankj.utilcode.util.EncryptUtils;

import java.io.*;

public class WebRTCManager {
    private static final String TAG = "WebRTCManager";

    private String clientId;
    private WebRTCCallback callback;
    private boolean isConnected = false;
    private String currentRoomId;

    public WebRTCManager(WebRTCCallback callback) {
        this.callback = callback;
    }

    /**
     * 加入房间
     * @param roomId 房间ID
     * @param serverUrl 信令服务器地址
     * @param debug 是否开启调试模式
     * @return 是否成功
     */
    public boolean joinRoom(String roomId, String serverUrl, boolean debug) {
        try {
            Log.d(TAG, "正在加入房间: " + roomId);

            // 创建客户端
            clientId = RustApi.createWebRTCClient(roomId, serverUrl, debug, callback);
            if (clientId == null || clientId.isEmpty()) {
                Log.e(TAG, "创建WebRTC客户端失败");
                return false;
            }

            Log.d(TAG, "WebRTC客户端创建成功，ID: " + clientId);

            // 连接到服务器
            isConnected = RustApi.connect(clientId);
            if (isConnected) {
                currentRoomId = roomId;
                Log.d(TAG, "成功连接到服务器");
            } else {
                Log.e(TAG, "连接服务器失败");
            }

            return isConnected;
        } catch (Exception e) {
            Log.e(TAG, "加入房间时发生异常", e);
            return false;
        }
    }

    /**
     * 发送文本消息
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String message) {
        if (!isConnected || clientId == null) {
            Log.w(TAG, "未连接，无法发送消息");
            return false;
        }

        Log.d(TAG, "发送文本消息: " + message);
        return RustApi.sendMessage(clientId, message);
    }

    /**
     * 发送二进制数据
     * @param data 二进制数据
     * @return 是否发送成功
     */
    public boolean sendBinaryData(byte[] data) {
        if (!isConnected || clientId == null) {
            Log.w(TAG, "未连接，无法发送数据");
            return false;
        }


        Log.d(TAG, "发送二进制数据: " + data.length + " 字节");
        return RustApi.sendBinaryData(clientId, data);
    }

    /**
     * 发送图片
     * @param bitmap 图片
     * @return 是否发送成功
     */
    public boolean sendImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();
            baos.close();

            Log.d(TAG, "发送图片: " + imageData.length + " 字节");
            return sendBinaryData(imageData);
        } catch (IOException e) {
            Log.e(TAG, "发送图片失败", e);
            return false;
        }
    }

    /**
     * 发送文件
     * @param filePath 文件路径
     * @return 是否发送成功
     */
    public boolean sendFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Log.w(TAG, "文件不存在: " + filePath);
                return false;
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fis.read(fileData);
            fis.close();

            Log.d(TAG, "发送文件: " + file.getName() + ", " + fileData.length + " 字节");
            return sendBinaryData(fileData);
        } catch (IOException e) {
            Log.e(TAG, "发送文件失败", e);
            return false;
        }
    }

    /**
     * 发送输入流数据
     * @param inputStream 输入流
     * @return 是否发送成功
     */
    public boolean sendInputStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            byte[] data = baos.toByteArray();
            baos.close();

            Log.d(TAG, "发送输入流数据: " + data.length + " 字节");
            return sendBinaryData(data);
        } catch (IOException e) {
            Log.e(TAG, "发送输入流数据失败", e);
            return false;
        }
    }

    /**
     * 离开房间
     */
    public void leaveRoom() {
        if (clientId != null) {
            Log.d(TAG, "离开房间: " + currentRoomId);
            RustApi.close(clientId);
            clientId = null;
            isConnected = false;
            currentRoomId = null;
        }
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 获取客户端ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取当前房间ID
     */
    public String getCurrentRoomId() {
        return currentRoomId;
    }
}