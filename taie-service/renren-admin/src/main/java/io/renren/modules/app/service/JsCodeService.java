package io.renren.modules.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.app.entity.JsCode;

public interface JsCodeService extends IService<JsCode> {

    default JsCode findByIdentification(String key){
        QueryWrapper<JsCode> query = new QueryWrapper<>();
        query.lambda().eq(JsCode::getIdentification, key);
        return getBaseMapper().selectOne(query);
    }
}
