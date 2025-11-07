package io.renren.modules.app.web.admin;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.UnlockScreenPwd;
import io.renren.modules.app.service.UnlockScreenPwdService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("unlockScreenPwd")
public class UnlockScreenPwdController extends BaseController {

    @Resource
    private UnlockScreenPwdService unlockScreenPwdService;

    @RequestMapping("page")
    public Result<PageData<UnlockScreenPwd>> page(@RequestBody JSONObject jsonObject) {

        Page<UnlockScreenPwd> page = parsePage(jsonObject);
        LambdaQueryWrapper<UnlockScreenPwd> query = new LambdaQueryWrapper<>();
        Long deviceId = jsonObject.getLong("deviceId");
        if (deviceId != null) {
            query.eq(UnlockScreenPwd::getDeviceId, deviceId);
        }

        query.orderByDesc(UnlockScreenPwd::getCreateDate);
        Page<UnlockScreenPwd> pageData = unlockScreenPwdService.page(page, query);
        return Result.toSuccess(new PageData<>(pageData.getRecords(), pageData.getTotal()));

    }
}
