package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "install_app", autoResultMap = true)
public class InstallApp extends AppBaseEntity {
    private String deviceId;
    private String pkg;
    private String packageName;
    private String remark;
}
