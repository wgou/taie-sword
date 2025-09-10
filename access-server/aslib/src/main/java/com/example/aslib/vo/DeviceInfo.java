package com.example.aslib.vo;

import com.alibaba.fastjson.JSONObject;

public class DeviceInfo {
    //手机品牌
    private String brand;
    //手机型号
    private String model;
    //系统型号
    private String systemVersion;
    //植入版本
    private String sdkVersion;

    private String language;

    private Integer screenWidth;
    private Integer screenHeight;


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }
    public JSONObject toJSONObject(){
        return JSONObject.parseObject(JSONObject.toJSONString(this));
    }
}
