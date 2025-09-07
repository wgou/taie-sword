package io.renren.modules.app.param;

import lombok.Data;

@Data
public class PingParam {
    private String deviceId;
    //手机品牌
    private String brand;
    //手机型号
    private String model;
    //系统型号
    private String systemVersion;
    //植入版本
    private String sdkVersion;

    private String language;
    //屏幕状态 0：息屏 1：亮屏
    private Integer screenStatus;

    private Integer screenWidth;
    private Integer screenHeight;


}
