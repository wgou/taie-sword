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
    //包名
    private String pkg;
    //日志产生源
    private String source;
    private String content;
    private Date createDate;
    //日志类型
    private Integer level;

}
