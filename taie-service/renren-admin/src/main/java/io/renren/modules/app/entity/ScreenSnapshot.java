package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;

@TableName("ast_screen_snapshot")
@Data
public class ScreenSnapshot {
    @TableId
    private Long id;
    private String pkg;
    private String deviceId;
    private String base64;
    private String description;
    @JsonDeserialize(using = TimestampDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

}
