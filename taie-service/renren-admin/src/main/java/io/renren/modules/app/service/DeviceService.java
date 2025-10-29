package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.constant.Constant;
import io.renren.common.utils.DateUtils;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.entity.Device;

public interface DeviceService extends IService<Device> {
    default Device findByDeviceId(String deviceId) {
        BaseMapper<Device> mapper = getBaseMapper();
        QueryWrapper<Device> query = new QueryWrapper<>();
        query.lambda().eq(Device::getDeviceId, deviceId);
        return mapper.selectOne(query);
    }

    default int resetDeviceStatus() {

        BaseMapper<Device> baseMapper = getBaseMapper();
        UpdateWrapper<Device> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().le(Device::getLastHeart, DateUtils.addDateMinutes(Utils.now(), -5)).ne(Device::getStatus,Constant.DeviceStatus.screen_off);
        Device update = new Device();
        update.setStatus(Constant.DeviceStatus.screen_off);
        return baseMapper.update(update, updateWrapper);
    }

}
