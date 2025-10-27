package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.common.service.BaseService;
import io.renren.modules.app.entity.InstallApp;

public interface InstallAppService extends IService<InstallApp> {
    default int deleteByDeviceIdAndPkg(String deviceId, String pkg){
        BaseMapper<InstallApp> baseMapper = getBaseMapper();
        LambdaQueryWrapper<InstallApp> query = new LambdaQueryWrapper<>();
        query.eq(InstallApp::getDeviceId, deviceId).eq(InstallApp::getPkg, pkg);
        return baseMapper.delete(query);
    }
}
