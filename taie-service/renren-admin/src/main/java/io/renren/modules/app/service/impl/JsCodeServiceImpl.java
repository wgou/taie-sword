package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.JsCode;
import io.renren.modules.app.mapper.JsCodeMapper;
import io.renren.modules.app.service.JsCodeService;
import org.springframework.stereotype.Service;

@Service
public class JsCodeServiceImpl extends ServiceImpl<JsCodeMapper, JsCode> implements JsCodeService {
}
