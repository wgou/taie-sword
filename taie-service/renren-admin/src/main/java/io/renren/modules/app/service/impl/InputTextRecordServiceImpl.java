package io.renren.modules.app.service.impl;

import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.commons.dynamic.datasource.annotation.DataSource;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.vo.InputTextGroup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DataSource("clickhouse")
public class InputTextRecordServiceImpl extends BaseServiceImpl<InputTextRecordMapper, InputTextRecord> implements InputTextRecordService {
    
    @Resource
    private InputTextRecordMapper inputTextRecordMapper;
    
    @Override
    public List<InputTextGroup> queryGroupedRecords(String deviceId, Long startTime, Long endTime, String appPkg) {
        // 查询原始数据
        List<InputTextRecord> records = inputTextRecordMapper.queryForGroup(deviceId, startTime, endTime, appPkg);
        
        // 按包名分组
        Map<String, List<InputTextRecord>> groupedByPkg = records.stream()
                .collect(Collectors.groupingBy(InputTextRecord::getPkg));
        
        // 转换为InputTextGroup对象
        List<InputTextGroup> result = new ArrayList<>();
        for (Map.Entry<String, List<InputTextRecord>> entry : groupedByPkg.entrySet()) {
            String pkg = entry.getKey();
            List<InputTextRecord> recordList = entry.getValue();
            
            InputTextGroup group = new InputTextGroup();
            group.setPkg(pkg);
            group.setDeviceId(deviceId);
            
            // 转换为Item对象
            List<InputTextGroup.Item> items = recordList.stream()
                    .map(record -> {
                        InputTextGroup.Item item = new InputTextGroup.Item();
                        item.setApp(record.getAppPkg());
                        item.setResourceId(record.getResourceId());
                        item.setText(record.getText());
                        item.setDate(new Date(record.getTime()));
                        return item;
                    })
                    .collect(Collectors.toList());
            
            group.setItems(items);
            result.add(group);
        }
        
        return result;
    }
}
