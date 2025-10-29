package io.renren.modules.app.jobs;

import io.renren.modules.app.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CommonJobs {

    @Resource
    private DeviceService deviceService;

    /**
     * 重置设备状态任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void resetDeviceStatus() {
        log.info("开始重置设备状态");
        int count = deviceService.resetDeviceStatus();
        log.info("重置{}个设备状态", count);

    }

}
