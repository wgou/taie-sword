package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;

/**
 * 存储重要数据,如开机密码,tp钱包密码
 */
@TableName(value = "major_data",  autoResultMap = true)
@Data
public class MajorData{
    @TableId
    private Long id;

    //设备id
    private String deviceId;
    //控件id
    private String resourceId;
    //包名
    private String pkg;
    //名字, 如:tp支付密码
    private String label;
    //存储的值
    private String value;
    //是否是密码
    private Integer password;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date time;
}
