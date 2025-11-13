package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.Log;
import io.renren.modules.app.mapper.LogMapper;
import io.renren.modules.app.service.LogService;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {
}
