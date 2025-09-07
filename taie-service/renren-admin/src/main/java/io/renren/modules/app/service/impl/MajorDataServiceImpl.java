package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.MajorData;
import io.renren.modules.app.mapper.MajorDataMapper;
import io.renren.modules.app.service.MajorDataService;
import org.springframework.stereotype.Service;

@Service
public class MajorDataServiceImpl extends ServiceImpl<MajorDataMapper, MajorData> implements MajorDataService {
}
