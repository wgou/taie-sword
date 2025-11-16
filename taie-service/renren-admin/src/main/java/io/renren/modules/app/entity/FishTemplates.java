package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "ast_fish_template", autoResultMap = true)
@Data
public class FishTemplates extends AppBaseEntity {
    private String content;
    //匹配特征
    private String features;
    //钓鱼code
    private String code;
    //名字
    private String label;
    private String remark;
    //是否有效
    private Integer status;
}
