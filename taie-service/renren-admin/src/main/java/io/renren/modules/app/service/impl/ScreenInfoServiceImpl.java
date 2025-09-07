package io.renren.modules.app.service.impl;

import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.commons.dynamic.datasource.annotation.DataSource;
import io.renren.modules.app.entity.ScreenInfo;
import io.renren.modules.app.mapper.ScreenInfoMapper;
import io.renren.modules.app.service.ScreenInfoService;
import org.springframework.stereotype.Service;

@Service
@DataSource("clickhouse")
public class ScreenInfoServiceImpl extends BaseServiceImpl<ScreenInfoMapper, ScreenInfo> implements ScreenInfoService {
}
