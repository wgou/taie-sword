package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.app.entity.MajorData;

public interface MajorDataService extends IService<MajorData> {

    default MajorData findByDeviceIdAndResourceId(String deviceId, String resourceId) {
        QueryWrapper<MajorData> query = new QueryWrapper<>();
        LambdaQueryWrapper<MajorData> lambda = query.lambda();
        lambda.eq(MajorData::getDeviceId, deviceId).eq(MajorData::getResourceId, resourceId);
        return getBaseMapper().selectOne(query);
    }
}
