package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 日志
 */
@Data
@TableName("log")
public class Log {
    @TableId
    private Long id;

    private String deviceId;
    //app名字
    private String appName;
    //包名
    private String packageName;
    //日志产生源
    private String source;
    private String text;
    private Date createTime;
    //日志类型
    private String type;

}
