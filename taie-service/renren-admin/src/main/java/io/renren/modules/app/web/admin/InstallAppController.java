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
import io.renren.modules.app.param.InstallAppPageParam;
import io.renren.modules.app.service.InstallAppService;

@RestController
@RequestMapping("installApp")
public class InstallAppController extends BaseController {
    @Resource
    private InstallAppService installAppService;

    @RequestMapping("list")
    public Result<PageData<InstallApp>>  list(@RequestBody InstallAppPageParam param) {
        Page<InstallApp> page = new Page<>(param.getPage(),param.getLimit());
        LambdaQueryWrapper<InstallApp> lambda = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(param.getDeviceId())) {
        	lambda.eq(InstallApp::getDeviceId, param.getDeviceId());
        }
        if (StringUtils.isNotBlank(param.getPkg())) {
        	lambda.like(InstallApp::getPkg, param.getPkg());
        }
        if (StringUtils.isNotBlank(param.getPackageName())) {
        	lambda.like(InstallApp::getPackageName, param.getPackageName());
        }
        if (StringUtils.isNotBlank(param.getRemark())) {
        	lambda.like(InstallApp::getRemark, param.getRemark());
        }
        Page<InstallApp> pageData = installAppService.page(page, lambda);
        return Result.toSuccess(new PageData<InstallApp>(pageData.getRecords(), pageData.getTotal()));
    }
    
    
    @RequestMapping("addRemark")
    public Result<Void> addRemark(@RequestBody JSONObject jsonObject) {
        Long id = jsonObject.getLong("id");
        InstallApp installApp = installAppService.getById(id);
        if(installApp== null){
            return Result.toError("没有找到这个应用!");
        }
        String remark = jsonObject.getString("remark");
        InstallApp update = new InstallApp();
        update.setId(id);
        update.setRemark(remark);
        installAppService.updateById(update);
        return Result.toSuccess();
    }
}
