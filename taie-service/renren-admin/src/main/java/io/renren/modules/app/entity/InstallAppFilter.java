package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "ast_install_app_filter",  autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class InstallAppFilter {
    private Long id;
    private String appName;
    private String packageName;
}
