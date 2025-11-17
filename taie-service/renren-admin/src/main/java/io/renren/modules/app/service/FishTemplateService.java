package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.constant.Constant;
import io.renren.modules.app.entity.FishTemplates;

import java.util.List;

public interface FishTemplateService extends IService<FishTemplates> {

    default List<FishTemplates> effectiveFishTemplates(){
        LambdaQueryWrapper<FishTemplates> query = new LambdaQueryWrapper<>();
        query.eq(FishTemplates::getStatus, Constant.FishTemplatesStatus.effective);
        return list(query);
    }
}
