package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.entity.Asset;
import io.renren.modules.app.mapper.AssetMapper;
import io.renren.modules.app.service.AssetService;
import org.springframework.stereotype.Service;

@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements AssetService {
}
