package io.renren.modules.app.web.admin;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.InstallApp;
import io.renren.modules.app.service.InstallAppService;

@RestController
@RequestMapping("installApp")
public class InstallAppController extends BaseController {
    @Resource
    private InstallAppService installAppService;

    @RequestMapping("list")
    public Result<PageData<InstallApp>>  list(@RequestBody JSONObject jsonObject) {
        Page<InstallApp> page = parsePage(jsonObject);
        LambdaQueryWrapper<InstallApp> lambda = new LambdaQueryWrapper<>();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotBlank(deviceId)) {
        	lambda.eq(InstallApp::getDeviceId, deviceId);
        }
        String packageName = jsonObject.getString("packageName");
        if (StringUtils.isNotBlank(packageName)) {
        	lambda.like(InstallApp::getPackageName, packageName);
        }
        Page<InstallApp> pageData = installAppService.page(page, lambda);
        return Result.toSuccess(new PageData<InstallApp>(pageData.getRecords(), pageData.getTotal()));
    }
}
