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
	private String id;
    private String deviceId;
    private String appName;
    private String packageName;

    private Integer password;
    private String text;
    private String time;
    private String date;
}
