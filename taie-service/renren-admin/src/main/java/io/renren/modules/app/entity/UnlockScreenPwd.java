package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

/**
 * 解锁密码
 */
@Data
@TableName(value = "unlock_screen_pwd", autoResultMap = true)
public class UnlockScreenPwd {

    @TableId
    private Long id;
    /**
     * pkg
     */
    private String pkg;
    /**
     * device 表主键id
     */
    private Long deviceId;
    /**
     * 设备id
     */
    private String androidId;

    private int type;
    private String value;
    private String tips;
    private String resourceId;
    private int source;
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}
