package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.AppTransferRecord;
import io.renren.modules.app.mapper.AppTransferRecordMapper;
import io.renren.modules.app.service.AppTransferRecordService;
@Service
public class AppTransferRecordServiceImpl extends ServiceImpl<AppTransferRecordMapper, AppTransferRecord>  implements AppTransferRecordService {

}
