package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.InstallApp;
import io.renren.modules.app.mapper.InstallAppMapper;
import io.renren.modules.app.service.InstallAppService;

@Service
public class InstallAppServiceImpl extends ServiceImpl<InstallAppMapper, InstallApp> implements InstallAppService {
}
