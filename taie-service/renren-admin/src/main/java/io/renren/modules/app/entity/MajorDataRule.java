package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 重要数据规则
 */
@Data
@TableName(value = "major_data_rule",  autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
public class MajorDataRule {

    private Long id;
    //资源id
    private String feature;
    //应用名称
    private String appName;
    //包名
    private String packageName;

    private Date createDate;


}
