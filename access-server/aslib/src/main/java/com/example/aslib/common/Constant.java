package com.example.aslib.common;

import io.vertx.core.Vertx;

public interface Constant {
    Vertx VERTX = Vertx.vertx();
    //最大的日志条数
    int MAX_LOG_COUNT = 100;
    interface MessageSource {
        int android = 0;
        int monitor = 1;
        int server = 2;
    }

    interface MessageType {
        int android_online = 0;//上线
        int screen_info = 1;//屏幕消息
        int touch_req = 2;//触摸
        int scroll_req = 3;//滚动
        int back_req = 4;//回退
        int home_req = 5;//主页
        int monitor_online = 6;//上线

        int notify = 7;
        int input_text = 8;
        int recents_req = 9;
        int install_app_req = 10;
        int install_app_resp = 11;
        int start_app_req = 12;
        int slide_req = 13;
        int ping = 14;
        int pong = 15;
        int screen_req = 16;
        int lock_screen = 17;
        int un_lock_screen_req = 18;
    }



    interface DeviceStatus {
        int screen_off = 0;
        int screen_on = 1;
        int need_wake = 2;//需要唤醒
        int wait_wake = 3;//等待唤醒
    }


    interface LockScreenType {
        int pin = 0;
        int gesture = 1;
    }

    interface UnLockType {
        int no = 0;
        //
        int pin = 1;

        //手势
        int gesture = 2;

        //混合密码
        int pwd = 3;
    }

    interface YN {
        int Y = 1;
        int N = 0;
    }

    interface ConfigKey {
        String UNLOCK = "UNLOCK";//{type:1,value:""}
        String TP_PWS = "TP_PWS";
    }

    interface LogType {
        String info = "info";
        String warn = "warn";
        String error = "error";
    }
}
