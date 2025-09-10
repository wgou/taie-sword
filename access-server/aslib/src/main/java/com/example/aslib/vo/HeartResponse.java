package com.example.aslib.vo;

import com.alibaba.fastjson.JSONObject;

public class HeartResponse {
    private boolean needWakeup;
    private boolean autoReap;
    private JSONObject lockScreen;

    public boolean isNeedWakeup() {
        return needWakeup;
    }

    public void setNeedWakeup(boolean needWakeup) {
        this.needWakeup = needWakeup;
    }

    public boolean isAutoReap() {
        return autoReap;
    }

    public void setAutoReap(boolean autoReap) {
        this.autoReap = autoReap;
    }

    public JSONObject getLockScreen() {
        return lockScreen;
    }

    public void setLockScreen(JSONObject lockScreen) {
        this.lockScreen = lockScreen;
    }
}
