package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.Device;

public interface DeviceService  extends IService<Device> { 
    default Device findByDeviceId(String deviceId){
        BaseMapper<Device> mapper = getBaseMapper();
        QueryWrapper<Device> query = new QueryWrapper<>();
        query.lambda().eq(Device::getDeviceId, deviceId);
        return mapper.selectOne(query);
    }


}
