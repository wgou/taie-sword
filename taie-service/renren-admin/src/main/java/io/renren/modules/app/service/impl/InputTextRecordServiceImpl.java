package io.renren.modules.app.service.impl;

import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.commons.dynamic.datasource.annotation.DataSource;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.service.InputTextRecordService;
import org.springframework.stereotype.Service;

@Service
@DataSource("clickhouse")
public class InputTextRecordServiceImpl extends BaseServiceImpl<InputTextRecordMapper, InputTextRecord> implements InputTextRecordService {
}
