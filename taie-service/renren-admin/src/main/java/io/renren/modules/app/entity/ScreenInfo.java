package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * clickhouse
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "ast_screen_info", autoResultMap = true)
public class ScreenInfo {
    private String deviceId;
    private String packageName;
    private String appName;
//    @TableField(typeHandler = FastjsonTypeHandler.class)
    private String items;//JSON格式
//    private Date createDate;
    private String time;
    private String date;
}
