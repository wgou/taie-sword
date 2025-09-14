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

    private String deviceId;
    private String pkg;

    /**
     * app包名
     */
    private String appPkg;

    /**
     * 控件id
     */
    private String resourceId;

    /**
     * 是否密码输入
     */
    private Integer password;
    /**
     * 内容
     */
    private String text;
    /**
     * 输入时间
     */
    private long time;
}
