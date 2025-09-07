package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.Device;
import io.renren.modules.app.mapper.DeviceMapper;
import io.renren.modules.app.service.DeviceService;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device>  implements DeviceService {
}
