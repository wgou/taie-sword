package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.app.entity.Asset;

public interface AssetService extends IService<Asset> {
    default Asset findByCondition(String deviceId, String appPkg, String currency, String name){
        BaseMapper<Asset> baseMapper = getBaseMapper();
        QueryWrapper<Asset> query = new QueryWrapper<>();
        LambdaQueryWrapper<Asset> lambda = query.lambda();
        lambda.eq(Asset::getDeviceId, deviceId);
        lambda.eq(Asset::getAppPkg, appPkg);
        lambda.eq(Asset::getCurrency, currency);
        lambda.eq(Asset::getName, name);
        return baseMapper.selectOne(query);
    }
}
