package io.renren.modules.app.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.renren.common.exception.RenException;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.commons.dynamic.datasource.annotation.DataSource;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.message.proto.Message.InputText;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.vo.InputTextGroup;

@Service
@DataSource("clickhouse")
public class InputTextRecordServiceImpl extends BaseServiceImpl<InputTextRecordMapper, InputTextRecord> implements InputTextRecordService {
    
    @Resource
    private InputTextRecordMapper inputTextRecordMapper;
    
    @Override
    public InputTextGroup queryGroupedRecords(JSONObject jsonObject) {
    	 
          if (StringUtils.isEmpty(jsonObject.getString("deviceId"))) {
              throw new RenException("设备ID不能为空!");
          }
          if (jsonObject.getLong("startTime") == null || jsonObject.getLong("endTime") == null) {
              throw new RenException("时间范围不能为空!");
          }
          if (StringUtils.isEmpty(jsonObject.getString("pkg"))) {
              throw new RenException("包名不能为空!");
          }
          InputTextGroup group = new InputTextGroup();
          group.setPkg(jsonObject.getString("pkg"));
          group.setDeviceId(jsonObject.getString("deviceId"));
          
          QueryWrapper<InputTextRecord> query =  new QueryWrapper<InputTextRecord>();
          query.eq("device_id", jsonObject.getString("deviceId"));
          query.eq("pkg", jsonObject.getString("pkg"));
          query.ge("time", jsonObject.getLong("startTime"));
          query.le("time", jsonObject.getLong("endTime"));
          if(StringUtils.isNoneBlank(jsonObject.getString("appPkg"))) {
        	  query.eq("app_pkg", jsonObject.getString("appPkg"));
          }
          if(jsonObject.getInteger("source") !=null) {
        	  query.eq("source", jsonObject.getInteger("source"));
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
    	//TODO 后台Input_Text 消息体 增加 
    	inputRecord.setPkg(null);
    	inputRecord.setAppPkg(null);
    	inputRecord.setPassword(null);
    	
    	inputRecord.setSource(1);
    	inputRecord.setText(inputText.getText());
    	inputRecord.setResourceId(inputText.getId());
    	inputRecord.setTime(System.currentTimeMillis());
    	inputTextRecordMapper.insert(inputRecord);
    	
    }
}
