package io.renren.modules.app.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class DeviceStatus {
    //屏幕状态 0：息屏 1：亮屏
    private Integer screenStatus;

    private boolean accessibilityServiceEnabled;

    private String jsCodeMd5;
    //权限 {"android.permission.READ_MEDIA_IMAGES":boolean}
    private JSONObject permissions;

    //电量
    private int battery;

    //是否充电
    private boolean charging;

    //base64 屏幕快照
    private String screenSnapshot;
}
