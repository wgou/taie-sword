package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;

@TableName("asset")
@Data
public class Asset {
    @TableId
    private Long id;
    private String deviceId;
    private String pkg;

    private String appPkg;


    private Double amount;
    private String currency;//如果是total 则为 ALL
    private String name;
    private Double price;
    private String title;
    private String token;//币地址
    private String unit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = TimestampDeserializer.class)
    private Date time;
}
