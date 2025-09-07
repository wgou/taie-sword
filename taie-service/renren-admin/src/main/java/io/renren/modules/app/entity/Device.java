package io.renren.modules.app.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.entity.BaseEntity;
import io.renren.common.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "device", autoResultMap = true)
public class Device extends BaseEntity {
    private String deviceId;
    //手机型号
    private String model;

    private String language;
    private String systemVersion;
    private String sdkVersion;
    private String brand;

    private Integer screenWidth;
    private Integer screenHeight;
    //分辨率?
    private String ip;

    //链接 状态 0：离线  1：在线
    private Integer status;

    //是否自动收割
    private Integer autoReap;

    //锁屏
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject lockScreen;

    //是否固定锁屏密码 手势可能存在不稳定
    private Integer fixLockScreen;
    
    //手动操作次数
    private Integer manualCount;
    //自动化操作次数
    private Integer autoCount;
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHeart;
    //资产信息
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject assets;
    //app密码信息
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject appPassword;
}
