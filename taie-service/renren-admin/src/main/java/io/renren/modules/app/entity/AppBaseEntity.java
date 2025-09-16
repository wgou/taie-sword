package io.renren.modules.app.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.renren.common.utils.DateUtils;
import lombok.Data;
@Data
public class AppBaseEntity {
	 /**
     * id
     */
    @TableId
    private Long id;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date created;
    
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date modified;
}
