package io.renren.modules.app.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 输入文本记录查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InputTextRecordParam extends PkgParam {
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 应用包名
     */
    private String appPkg;
    
    /**
     * 来源：0-App端 1-管理端
     */
    private Integer source;
    
    /**
     * 开始时间（时间戳）
     */
    private Long startTime;
    
    /**
     * 结束时间（时间戳）
     */
    private Long endTime;
}

