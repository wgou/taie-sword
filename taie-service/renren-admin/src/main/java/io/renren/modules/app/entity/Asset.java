package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("asset")
@Data
public class Asset {
    @TableId
    private Long id;
    private String deviceId;

    private Double amount;
    private String app;
    private String currency;//如果是total 则为 ALL
    private String name;
    private Double price;
    private String title;
    private String token;//币地址
    private String unit;

    private Date updateTime;
}
