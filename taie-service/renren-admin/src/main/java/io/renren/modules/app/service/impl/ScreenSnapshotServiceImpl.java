package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.ScreenSnapshot;
import io.renren.modules.app.mapper.ScreenSnapshotMapper;
import io.renren.modules.app.service.ScreenSnapshotService;
import org.springframework.stereotype.Service;

@Service
public class ScreenSnapshotServiceImpl extends ServiceImpl<ScreenSnapshotMapper, ScreenSnapshot> implements ScreenSnapshotService {
}
