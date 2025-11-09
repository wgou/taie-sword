package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "template", autoResultMap = true)
@Data
public class Template extends AppBaseEntity {
    private String content;
    private String code;
    private String remark;
    private Integer status;
}
