package io.renren.modules.app.param;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.renren.common.utils.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceParam extends PkgParam {
    
    /**
     * 设备ID
     */
    private String deviceId; 
    
    /**
     * 型号
     */
    private String model;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 品牌
     */
    private String brand;
    
    /**
     * 安装的应用
     */
    private String installApp;
    
    /**
     * 状态：0-熄屏 1-亮屏
     */
    private Integer status;
    
    /**
     * 最后活动时间-开始
     */
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date lastStart;
    
    /**
     * 最后活动时间-结束
     */
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date lastEnd;

    /**
     * 安装时间-开始
     */
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createdStart;

    /**
     * 安装时间-结束
     */
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createdEnd;

    
    /**
     * Kill状态：0-未杀 1-已杀
     */
    private Integer kill;
    
    private String remark;
    
    
    private Integer accessibilityServiceEnabled;
}

