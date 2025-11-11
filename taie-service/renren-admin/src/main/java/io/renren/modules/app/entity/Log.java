package io.renren.modules.app.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

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
    
    // 支持时间戳格式输入，自动转换为Date类型
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    
    //日志类型
    private Integer level;
}
