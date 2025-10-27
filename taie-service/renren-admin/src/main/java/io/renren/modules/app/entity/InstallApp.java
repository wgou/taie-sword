package io.renren.modules.app.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;

import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "install_app", autoResultMap = true)
public class InstallApp extends BaseEntity {
    private String deviceId;
    private String pkg;
    private String appName;
    private String packageName;
    
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject password;  //密码信息
    
    private String remark;
    private Date modified;
}
