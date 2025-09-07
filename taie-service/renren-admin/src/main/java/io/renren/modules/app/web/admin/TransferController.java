package io.renren.modules.app.web.admin;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.Transfer;
import io.renren.modules.app.service.TransferService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("transfer")
@RestController
public class TransferController extends BaseController{
    @Resource
    private TransferService transferService;
    @RequestMapping("page")
    public Result<PageData<Transfer>> page(@RequestBody JSONObject jsonObject) {
        Page<Transfer> page = parsePage(jsonObject);
        QueryWrapper<Transfer> query = new QueryWrapper<>();
        LambdaQueryWrapper<Transfer> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotEmpty(deviceId)) {
            lambda.eq(Transfer::getDeviceId, deviceId);
        }

        query.orderByDesc("submit_time");
        Page<Transfer> pageData = transferService.page(page, lambda);
        return Result.toSuccess(new PageData<>(pageData.getRecords(), pageData.getTotal()));
    }
}
