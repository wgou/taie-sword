package io.renren.modules.app.web.admin;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.Log;
import io.renren.modules.app.param.LogParam;
import io.renren.modules.app.service.LogService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("logInfo")
@Slf4j
public class LogInfoController extends BaseController{


	@Resource
	LogService logService;
	
	
    @RequestMapping("page")
    public Result<PageData<Log>> page(@RequestBody LogParam param) {
        Page<Log> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<Log> query = new QueryWrapper<>();
        LambdaQueryWrapper<Log> lambda = query.lambda();
        if (StringUtils.isNotEmpty(param.getDeviceId())) {
            lambda.eq(Log::getDeviceId, param.getDeviceId());
        }
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(Log::getPkg, param.getPkg());
        }
        if (StringUtils.isNotEmpty(param.getContent())) {
        	lambda.like(Log::getContent, param.getContent());
        }
        if(StringUtils.isNotEmpty(param.getSource())){
            lambda.eq(Log::getSource, param.getSource());
        }
        lambda.orderByDesc(Log::getTime);
      
        Page<Log> pageData = logService.page(page, lambda);
        return Result.toSuccess(new PageData<Log>(pageData.getRecords(), pageData.getTotal()));
    }
}
