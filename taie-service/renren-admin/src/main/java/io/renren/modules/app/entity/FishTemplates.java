package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "fish_template", autoResultMap = true)
@Data
public class FishTemplates extends AppBaseEntity {
    private String content;
    //匹配特征
    private String features;
    private String code;
    private String label;
    private String remark;
    private Integer status;
}
