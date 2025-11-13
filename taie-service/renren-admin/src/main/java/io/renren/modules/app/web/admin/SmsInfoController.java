package io.renren.modules.app.web.admin;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.SmsInfoEntity;
import io.renren.modules.app.service.SmsInfoService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("smsInfo")
@Slf4j
public class SmsInfoController extends BaseController{


	@Resource
	SmsInfoService smsInfoService;
	
	
    @RequestMapping("page")
    public Result<PageData<SmsInfoEntity>> page(@RequestBody JSONObject jsonObject) {
        Page<SmsInfoEntity> page = parsePage(jsonObject);
        QueryWrapper<SmsInfoEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<SmsInfoEntity> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotEmpty(deviceId)) {
            lambda.eq(SmsInfoEntity::getDeviceId, deviceId);
        }
        if (StringUtils.isNotEmpty(jsonObject.getString("pkg"))) {
            lambda.eq(SmsInfoEntity::getPkg, jsonObject.getString("pkg"));
        }
        String content = jsonObject.getString("content");
        lambda.like(SmsInfoEntity::getContent, content);
      
        Page<SmsInfoEntity> pageData = smsInfoService.page(page, lambda);
        return Result.toSuccess(new PageData<SmsInfoEntity>(pageData.getRecords(), pageData.getTotal()));
    }
}
