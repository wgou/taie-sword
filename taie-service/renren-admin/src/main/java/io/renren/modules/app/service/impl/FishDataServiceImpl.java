package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.FishData;
import io.renren.modules.app.mapper.FishDataMapper;
import io.renren.modules.app.service.FishDataService;
import org.springframework.stereotype.Service;

@Service
public class FishDataServiceImpl extends ServiceImpl<FishDataMapper, FishData> implements FishDataService {
}
