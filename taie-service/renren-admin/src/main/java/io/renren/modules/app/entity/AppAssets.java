package io.renren.modules.app.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "app_assets", autoResultMap = true)
public class AppAssets extends BaseEntity {
    private String deviceId;
    private String appName;
    private String packageName;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject assets;  //资产信息

    private String remark;
    private Date modified;
}
