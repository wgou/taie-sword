package io.renren.modules.app.service;

import io.renren.common.service.BaseService;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.vo.InputTextGroup;

import java.util.List;

public interface InputTextRecordService extends BaseService<InputTextRecord> {
    
    /**
     * 聚合查询输入日志
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param appPkg app包名
     * @return 聚合后的输入日志分组
     */
    List<InputTextGroup> queryGroupedRecords(String deviceId, Long startTime, Long endTime, String appPkg);
}
