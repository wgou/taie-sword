# 二进制数据传输使用指南

本指南展示如何在 Android WebRTC 客户端中发送和接收二进制数据。

## Java 端接口定义

### 1. 更新回调接口

```java
public interface WebRTCCallback {
    void onConnected(String clientId);
    void onUserJoined(String userId);
    void onUserLeft(String userId);
    void onMessageReceived(String senderId, String message, boolean isP2P);
    void onBinaryDataReceived(String senderId, byte[] data, boolean isP2P); // 新增二进制数据回调
    void onConnectionStateChanged(String state);
    void onError(String error);
}
```

### 2. 更新 RustApi 类

```java
public class RustApi {
    static {
        System.loadLibrary("rust_jni");
    }

    // WebRTC 相关方法
    public static native String createWebRTCClient(
        String roomId, 
        String serverUrl, 
        boolean debug, 
        WebRTCCallback callback
    );

    public static native boolean connect(String clientId);
    
    // 发送文本消息
    public static native boolean sendMessage(String clientId, String message);
    
    // 发送二进制数据 - 新增方法
    public static native boolean sendBinaryData(String clientId, byte[] data);

    public static native boolean close(String clientId);

    // 保留原有方法
    public static native void send(byte[] data);
}
```

### 3. 更新 WebRTC 管理器

```java
public class WebRTCManager {
    private String clientId;
    private WebRTCCallback callback;
    private boolean isConnected = false;

    public WebRTCManager(WebRTCCallback callback) {
        this.callback = callback;
    }

    /**
     * 加入房间
     */
    public boolean joinRoom(String roomId, String serverUrl, boolean debug) {
        try {
            clientId = RustApi.createWebRTCClient(roomId, serverUrl, debug, callback);
            if (clientId == null || clientId.isEmpty()) {
                return false;
            }
            isConnected = RustApi.connect(clientId);
            return isConnected;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送文本消息
     */
    public boolean sendMessage(String message) {
        if (!isConnected || clientId == null) {
            return false;
        }
        return RustApi.sendMessage(clientId, message);
    }

    /**
     * 发送二进制数据 - 新增方法
     */
    public boolean sendBinaryData(byte[] data) {
        if (!isConnected || clientId == null) {
            return false;
        }
        return RustApi.sendBinaryData(clientId, data);
    }

    /**
     * 发送文件
     */
    public boolean sendFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
            
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int) file.length()];
            fis.read(fileData);
            fis.close();
            
            return sendBinaryData(fileData);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送图片
     */
    public boolean sendImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();
            baos.close();
            
            return sendBinaryData(imageData);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 离开房间
     */
    public void leaveRoom() {
        if (clientId != null) {
            RustApi.close(clientId);
            clientId = null;
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getClientId() {
        return clientId;
    }
}
```

## 使用示例

### 1. 基本使用

```java
public class MainActivity extends AppCompatActivity {
    private WebRTCManager webrtcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建回调实现
        WebRTCCallback callback = new WebRTCCallback() {
            @Override
            public void onConnected(String clientId) {
                runOnUiThread(() -> {
                    Log.d("WebRTC", "已连接，客户端ID: " + clientId);
                });
            }

            @Override
            public void onUserJoined(String userId) {
                runOnUiThread(() -> {
                    Log.d("WebRTC", "用户加入: " + userId);
                });
            }

            @Override
            public void onUserLeft(String userId) {
                runOnUiThread(() -> {
                    Log.d("WebRTC", "用户离开: " + userId);
                });
            }

            @Override
            public void onMessageReceived(String senderId, String message, boolean isP2P) {
                runOnUiThread(() -> {
                    String type = isP2P ? "P2P" : "转发";
                    Log.d("WebRTC", "收到" + type + "文本消息 [" + senderId + "]: " + message);
                    displayTextMessage(senderId, message, isP2P);
                });
            }

            @Override
            public void onBinaryDataReceived(String senderId, byte[] data, boolean isP2P) {
                runOnUiThread(() -> {
                    String type = isP2P ? "P2P" : "转发";
                    Log.d("WebRTC", "收到" + type + "二进制数据 [" + senderId + "]: " + data.length + " 字节");
                    handleBinaryData(senderId, data, isP2P);
                });
            }

            @Override
            public void onConnectionStateChanged(String state) {
                runOnUiThread(() -> {
                    Log.d("WebRTC", "连接状态变化: " + state);
                    updateConnectionStatus(state);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("WebRTC", "错误: " + error);
                    showError(error);
                });
            }
        };

        webrtcManager = new WebRTCManager(callback);

        setupButtons();
    }

    private void setupButtons() {
        // 加入房间
        findViewById(R.id.btnJoin).setOnClickListener(v -> {
            boolean success = webrtcManager.joinRoom("room123", "ws://192.168.1.100:9001", true);
            showToast(success ? "成功加入房间" : "加入房间失败");
        });

        // 发送文本消息
        findViewById(R.id.btnSendText).setOnClickListener(v -> {
            EditText editText = findViewById(R.id.editMessage);
            String message = editText.getText().toString();
            if (!message.isEmpty()) {
                boolean success = webrtcManager.sendMessage(message);
                if (success) {
                    editText.setText("");
                    displayTextMessage("我", message, false);
                } else {
                    showToast("发送失败");
                }
            }
        });

        // 发送二进制数据
        findViewById(R.id.btnSendBinary).setOnClickListener(v -> {
            // 示例：发送一些测试数据
            byte[] testData = "Hello Binary World!".getBytes();
            boolean success = webrtcManager.sendBinaryData(testData);
            showToast(success ? "二进制数据发送成功" : "二进制数据发送失败");
        });

        // 发送图片
        findViewById(R.id.btnSendImage).setOnClickListener(v -> {
            selectAndSendImage();
        });

        // 发送文件
        findViewById(R.id.btnSendFile).setOnClickListener(v -> {
            selectAndSendFile();
        });

        // 离开房间
        findViewById(R.id.btnLeave).setOnClickListener(v -> {
            webrtcManager.leaveRoom();
            showToast("已离开房间");
        });
    }

    private void handleBinaryData(String senderId, byte[] data, boolean isP2P) {
        // 根据数据内容判断类型并处理
        if (isImageData(data)) {
            displayReceivedImage(senderId, data, isP2P);
        } else if (isTextData(data)) {
            String text = new String(data);
            displayTextMessage(senderId, "[二进制文本] " + text, isP2P);
        } else {
            // 其他二进制数据
            displayBinaryInfo(senderId, data.length, isP2P);
        }
    }

    private boolean isImageData(byte[] data) {
        // 简单检查是否为图片数据（检查文件头）
        if (data.length < 4) return false;
        
        // PNG 文件头
        if (data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return true;
        }
        
        // JPEG 文件头
        if (data[0] == (byte)0xFF && data[1] == (byte)0xD8) {
            return true;
        }
        
        return false;
    }

    private boolean isTextData(byte[] data) {
        // 简单检查是否为文本数据
        try {
            new String(data, "UTF-8");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void displayReceivedImage(String senderId, byte[] imageData, boolean isP2P) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        if (bitmap != null) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            
            // 添加到消息列表或显示在对话框中
            String type = isP2P ? "P2P" : "转发";
            Log.d("WebRTC", "收到" + type + "图片 [" + senderId + "]");
            
            // 这里可以添加到聊天界面的图片显示逻辑
        }
    }

    private void displayTextMessage(String sender, String message, boolean isP2P) {
        TextView messagesView = findViewById(R.id.messagesView);
        String prefix = isP2P ? "[P2P]" : "[转发]";
        String text = messagesView.getText() + "\n" + prefix + sender + ": " + message;
        messagesView.setText(text);
    }

    private void displayBinaryInfo(String sender, int dataLength, boolean isP2P) {
        TextView messagesView = findViewById(R.id.messagesView);
        String prefix = isP2P ? "[P2P]" : "[转发]";
        String text = messagesView.getText() + "\n" + prefix + sender + ": [二进制数据 " + dataLength + " 字节]";
        messagesView.setText(text);
    }

    private void selectAndSendImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void selectAndSendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            
            if (requestCode == REQUEST_IMAGE) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    boolean success = webrtcManager.sendImage(bitmap);
                    showToast(success ? "图片发送成功" : "图片发送失败");
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("读取图片失败");
                }
            } else if (requestCode == REQUEST_FILE) {
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    byte[] fileData = readInputStream(is);
                    is.close();
                    
                    boolean success = webrtcManager.sendBinaryData(fileData);
                    showToast(success ? "文件发送成功" : "文件发送失败");
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("读取文件失败");
                }
            }
        }
    }

    private byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        return baos.toByteArray();
    }

    private void updateConnectionStatus(String status) {
        TextView statusView = findViewById(R.id.statusView);
        statusView.setText("状态: " + status);
    }

    private void showError(String error) {
        showToast("错误: " + error);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webrtcManager != null) {
            webrtcManager.leaveRoom();
        }
    }

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_FILE = 2;
}
```

## 数据类型支持

### 1. 支持的数据类型
- **文本消息**: 使用 `sendMessage()` 方法
- **二进制数据**: 使用 `sendBinaryData()` 方法
- **图片**: 使用 `sendImage()` 方法（内部转换为二进制）
- **文件**: 使用 `sendFile()` 方法（内部转换为二进制）

### 2. 传输特性
- **P2P优先**: 优先使用P2P直连传输，延迟低
- **自动回退**: P2P不可用时自动回退到服务器转发
- **类型区分**: 回调中可以区分P2P和服务器转发的数据
- **大小限制**: 建议单次传输不超过 64KB

### 3. 性能优化建议
- **大文件分片**: 大文件建议分片传输
- **压缩**: 图片等数据建议先压缩再发送
- **缓存**: 接收到的数据可以缓存避免重复处理
- **异步处理**: 数据处理建议在后台线程进行

## 注意事项

1. **线程安全**: 回调方法在后台线程执行，UI更新需要切换到主线程
2. **内存管理**: 大文件传输时注意内存使用
3. **网络状态**: 监听网络状态变化，适时重连
4. **错误处理**: 完善的错误处理和用户提示
5. **权限**: 确保应用有读取文件和网络访问权限

这样就完成了对二进制数据传输的完整支持！
