package io.renren.modules.app.web.admin;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.exception.RenException;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.InstallApp;
import io.renren.modules.app.service.InstallAppService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("installApp")
public class InstallAppController extends BaseController {
    @Resource
    private InstallAppService installAppService;

    @RequestMapping("list")
    public Result<List<InstallApp>> list(@RequestBody JSONObject jsonObject) {
        QueryWrapper<InstallApp> query = new QueryWrapper<>();
        LambdaQueryWrapper<InstallApp> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotBlank(deviceId)) {
        	lambda.eq(InstallApp::getDeviceId, deviceId);
        }
        String packageName = jsonObject.getString("packageName");
        if (StringUtils.isNotBlank(packageName)) {
        	lambda.like(InstallApp::getPackageName, packageName);
        }
        List<InstallApp> list = installAppService.list(query);
        return Result.toSuccess(list);
    }
}
