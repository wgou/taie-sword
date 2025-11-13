package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("fish_data")
public class FishData {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * pkg
     */
    private String pkg;
    /**
     * device 表主键id
     */
    private Long deviceId;
    /**
     * 设备id
     */
    private String androidId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
    //钓鱼code
    private String code;
    //钓鱼提交的数据
    private String data;
}
