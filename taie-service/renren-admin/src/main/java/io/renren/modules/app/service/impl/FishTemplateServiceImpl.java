package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.FishTemplates;
import io.renren.modules.app.mapper.FishTemplatesMapper;
import io.renren.modules.app.service.FishTemplateService;
import org.springframework.stereotype.Service;

@Service
public class FishTemplateServiceImpl extends ServiceImpl<FishTemplatesMapper, FishTemplates> implements FishTemplateService {
}
