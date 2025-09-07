package io.renren.modules.app.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
@Data
public class HeartResponse {
    private boolean needWakeup;
    private boolean autoReap;
    private JSONObject lockScreen;
}
