package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.AppAssets;
import io.renren.modules.app.mapper.AppAssetsMapper;
import io.renren.modules.app.service.AppAssetsService;
@Service
public class AppAssetsServiceImpl extends ServiceImpl<AppAssetsMapper, AppAssets>  implements AppAssetsService {

}
