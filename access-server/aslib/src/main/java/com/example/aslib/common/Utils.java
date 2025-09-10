package com.example.aslib.common;

import static android.content.Context.POWER_SERVICE;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.aslib.vo.Currency;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.renren.modules.app.message.proto.Message;
import io.vertx.core.buffer.Buffer;

public class Utils {
    public static Buffer encode(int type, MessageLite messageLite) {
        byte[] body = messageLite.toByteArray();
        byte[] data = Message.WsMessage.newBuilder()
                .setType(type)
                .setSource(Constant.MessageSource.android)
                //body需要进行压缩
                .setBody(ByteString.copyFrom(compress(body))).build().toByteArray();
        //整体需要继续压缩?
        return Buffer.buffer(data);
    }

    // GZIP 压缩，接受 byte[] 并返回 byte[]
    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    // GZIP 解压，接受 byte[] 并返回 byte[]
    public static byte[] decompress(byte[] compressedData) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }


    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {

        }
    }

    // 检查是否熄屏
    public static boolean isScreenOff(Context context) {
        // 获取 PowerManager
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

        // 在 Android Lollipop (API 20) 及以上，使用 isInteractive()
        return !powerManager.isInteractive(); // 如果返回 true，表示屏幕亮起
    }

    public static boolean isScreenOn(Context context) {
        return !isScreenOff(context);
    }

    public static String getAppNameFromPackageName(String packageName, Context context) {
        PackageManager packageManager = context.getPackageManager();
        String appName;
        try {
            // 获取应用信息
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            // 获取应用名称
            appName = packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            appName = "未知应用";
        }
        return appName;
    }

    @SuppressLint("HardwareIds")
    public static String deviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }


    public static void slideUp(AccessibilityService accessibilityService, int widthPixels, int heightPixels) {
        float x0 = (float) widthPixels / 2;
        float y0 = (float) heightPixels / 4 * 3;
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
                new GestureDescription.StrokeDescription(path, 0, 300)
        );
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
    }


    public static void slide(AccessibilityService accessibilityService, List<Message.Point> points) {
        Message.Point first = points.get(0);
        Path path = new Path();
        path.moveTo(first.getX(), first.getY());
        for (int i = 1; i < points.size(); i++) {
            Message.Point point = points.get(i);
            path.lineTo(point.getX(), point.getY());
        }

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(
                new GestureDescription.StrokeDescription(path, 0, 230)
        );
        accessibilityService.dispatchGesture(gestureBuilder.build(), null, null);
    }


    public static void touch(AccessibilityService accessibilityService, int x, int y) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L));
        GestureDescription gesture = builder.build();
        accessibilityService.dispatchGesture(gesture, null, null);
    }

    public static AccessibilityNodeInfo findNodeByTextOrDesc(AccessibilityNodeInfo accessibilityNodeInfo, String str, boolean z) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        if (!z && ((accessibilityNodeInfo.getText() != null && accessibilityNodeInfo.getText().toString().trim().equals(str)) || (accessibilityNodeInfo.getContentDescription() != null && accessibilityNodeInfo.getContentDescription().toString().trim().equals(str)))) {
            return accessibilityNodeInfo;
        }
        if (accessibilityNodeInfo.getChildCount() > 0) {
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo findNodeByTextOrDesc = findNodeByTextOrDesc(accessibilityNodeInfo.getChild(i), str, false);
                if (findNodeByTextOrDesc != null) {
                    return findNodeByTextOrDesc;
                }
            }
        }
        return null;

    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isNotEmpty(CharSequence str) {
        return str != null && str.length() > 0;
    }

    public static boolean isAddress(String str) {
        return !TextUtils.isEmpty(str) && str.matches("^[A-Za-z0-9]{20,}$");
    }


    private AccessibilityNodeInfo findNodeContainsDesc(AccessibilityNodeInfo accessibilityNodeInfo, String str, boolean z) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        if (!z && accessibilityNodeInfo.getContentDescription() != null && accessibilityNodeInfo.getContentDescription().toString().contains(str)) {
            return accessibilityNodeInfo;
        }
        if (accessibilityNodeInfo.getChildCount() > 0) {
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo findNodeContainsDesc = findNodeContainsDesc(accessibilityNodeInfo.getChild(i), str, false);
                if (findNodeContainsDesc != null) {
                    return findNodeContainsDesc;
                }
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId;
        if (accessibilityNodeInfo == null || (findAccessibilityNodeInfosByViewId = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str)) == null || findAccessibilityNodeInfosByViewId.isEmpty()) {
            return null;
        }
        return findAccessibilityNodeInfosByViewId.get(0);
    }


    public static AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo accessibilityNodeInfo, String str, boolean z) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        if (!z && accessibilityNodeInfo.getText() != null && accessibilityNodeInfo.getText().toString().trim().equals(str)) {
            return accessibilityNodeInfo;
        }
        if (accessibilityNodeInfo.getChildCount() > 0) {
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo findNodeByText = findNodeByText(accessibilityNodeInfo.getChild(i), str, false);
                if (findNodeByText != null) {
                    return findNodeByText;
                }
            }
        }
        return null;

    }

    public static AccessibilityNodeInfo findNodeByDesc(AccessibilityNodeInfo p0, String p1, boolean p2) {
        AccessibilityNodeInfo node;
        if (p0 == null) {
            return null;
        }
        if (!p2 && (p0.getContentDescription() != null && p0.getContentDescription().toString().trim().equals(p1))) {
            return p0;
        }
        if (p0.getChildCount() > 0) {
            int i = 0;
            while (i < p0.getChildCount()) {
                if ((node = findNodeByDesc(p0.getChild(i), p1, false)) != null) {
                    return node;
                }
                i = i + 1;
            }
        }
        return null;
    }

    public static String formatCurrency(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if (MyConstants.CURRENCY_LIST.contains(str)) {
            return str;
        }
        for (String str2 : MyConstants.CURRENCY_LIST) {
            if (str.matches("^" + str2 + "[^a-zA-Z]+.*")) {
                return str2;
            }
        }
        if (str.contains("TRON") || str.contains("TRC20") || str.contains("Tron")) {
            return MyConstants.TRX;
        }
        if (str.contains("ERC20") || str.contains("Ethereum")) {
            return MyConstants.ETH;
        }
        if (str.contains("BSC") || str.contains("BEP20")) {
            return MyConstants.BNB;
        }
        if (str.contains("Bitcoin")) {
            return MyConstants.BTC;
        }
        if (str.contains("Polygon")) {
            return MyConstants.MATIC;
        }
        if (str.contains("Solana")) {
            return MyConstants.SOL;
        }
        if (str.contains("Avalanche")) {
            return MyConstants.AVAX;
        }
        if (str.contains("Arbitrum")) {
            return MyConstants.ETH;
        }
        if (str.contains("Optimism")) {
            return MyConstants.OP;
        }
        if (str.contains("Tezos")) {
            return MyConstants.XTZ;
        }
        return null;
    }


    public static String findTextById(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        AccessibilityNodeInfo findNodeById;
        if (accessibilityNodeInfo == null || (findNodeById = findNodeById(accessibilityNodeInfo, str)) == null || findNodeById.getText() == null) {
            return null;
        }
        return findNodeById.getText().toString().trim();
    }

    private static String findTextByIdNoTrim(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        AccessibilityNodeInfo findNodeById;
        if (accessibilityNodeInfo == null || (findNodeById = findNodeById(accessibilityNodeInfo, str)) == null || findNodeById.getText() == null) {
            return null;
        }
        return findNodeById.getText().toString();
    }


    private boolean containsAppName(String str, String appName) {
        //TODO
//        return str != null && (str.contains(appName) || ((isHiddenIcon && str.contains(TRANS_ICON_NAME)) || (!TextUtils.isEmpty(settingIconName) && str.contains(settingIconName))));
        return false;
    }


    //禁止关掉无障碍
    public static void guardSmartAssistant(AccessibilityService accessibilityService) {
//        AccessibilityNodeInfo rootInActiveWindow = accessibilityService.getRootInActiveWindow();
//        //TODO 补齐
//        if (rootInActiveWindow == null || findNodeByTextOrDesc(rootInActiveWindow, "", true) == null || findNodeByText(rootInActiveWindow, "", true) == null) {
//            return;
//        }
//        accessibilityService.performGlobalAction(1);
//        accessibilityService.performGlobalAction(1);
    }

    //静止卸载 TODO 参考 MyManager::noUninstall
    public static void guardUninstall(AccessibilityService accessibilityService, AccessibilityEvent accessibilityEvent, String appName) {

    }


    public static void performTextInput(AccessibilityNodeInfo nodeInfo, String text) {
        // 设置文本到目标节点
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
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
    public static void performSwipe(AccessibilityService accessibilityService, float startX, float startY, float endX, float endY, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);  // 滑动起点
        path.lineTo(endX, endY);      // 滑动终点

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, duration);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(stroke)
                .build();

        accessibilityService.dispatchGesture(gesture, null, null); // 执行手势
    }

    public static Message.InstallAppResp getAllApp(AccessibilityService accessibilityService, String deviceId) {
        PackageManager packageManager = accessibilityService.getPackageManager();
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
        builder.setDeviceId(deviceId);

        return builder.build();

    }


    public static boolean matchWord(String target, String input) {
        // Escape square brackets and split the target string by "][" to get all substrings
        String[] keywords = target.replaceAll("\\[", "").split("]");

        boolean found = false;
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                System.out.println("匹配成功: " + keyword);
                found = true;
                break;
            }
        }
        return found;
    }

    public static Currency extractCurrencyAndNumber(String input) {
        input = input.replace(" ", "");
        Pattern pattern = Pattern.compile("([\\p{Sc}])([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String symbol = matcher.group(1); // 提取货币符号部分
            String numberString = matcher.group(2).replace(",", ""); // 提取数字部分并去掉逗号

            double number = Double.parseDouble(numberString); // 转换为数字

            return new Currency(symbol, number);
        } else {
            return null;
        }
    }

    public static Double extractNumber(String input) {
        // 使用正则表达式匹配数字部分
        Pattern pattern = Pattern.compile("([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {

                String str;
                if (matcher.groupCount() == 1) {
                    str = matcher.group(0);
                } else {
                    str = matcher.group(1);
                }
                String numberString = str.replace(",", ""); // 提取并去掉逗号

                return Double.parseDouble(numberString);

            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Currency extractNumberAndSymbol(String input) {
        // 使用正则表达式匹配数字部分和符号部分
        Pattern pattern = Pattern.compile("([-+]?[0-9]+(?:\\.[0-9]+)?)\\s*([A-Z]+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String numberString = matcher.group(1); // 提取数字部分
            String symbol = matcher.group(2); // 提取符号部分
            double number = Double.parseDouble(numberString); // 转换为数字
            return new Currency(symbol, number);
        } else {
            return null;
        }
    }


    public static Date now() {
        return new Date();
    }

    public static String nowStr() {
        // 获取当前日期
        Date currentDate = new Date();

        // 定义格式化模式
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
        // 格式化日期
        return formatter.format(currentDate);


    }


}
