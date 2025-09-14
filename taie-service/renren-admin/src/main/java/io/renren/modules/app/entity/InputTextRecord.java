package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 输入框记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "input_text_record", autoResultMap = true)
public class InputTextRecord {

    //resourceId
    private String deviceId;
    private String pkg;
    private String appPkg;

    private String resourceId;

    private Integer password;
    private String text;
    private long time;
}
