package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.DownloadStatistics;
import io.renren.modules.app.mapper.DownloadStatisticsMapper;
import io.renren.modules.app.service.DownloadStatisticsService;

@Service
public class DownloadStatisticsServiceImpl extends ServiceImpl<DownloadStatisticsMapper, DownloadStatistics> implements DownloadStatisticsService {
}

