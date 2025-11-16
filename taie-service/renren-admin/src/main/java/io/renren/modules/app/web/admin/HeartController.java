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
import io.renren.modules.app.entity.HeartEntity;
import io.renren.modules.app.param.HeartParam;
import io.renren.modules.app.service.HeartService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("heart")
@Slf4j
public class HeartController extends BaseController{


	@Resource
	HeartService heartService;
	
	
    @RequestMapping("page")
    public Result<PageData<HeartEntity>> page(@RequestBody HeartParam param) {
        Page<HeartEntity> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<HeartEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<HeartEntity> lambda = query.lambda();
        if (StringUtils.isNotEmpty(param.getDeviceId())) {
            lambda.eq(HeartEntity::getDeviceId, param.getDeviceId());
        }
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(HeartEntity::getPkg, param.getPkg());
        }
        lambda.orderByDesc(HeartEntity::getCreated);
      
        Page<HeartEntity> pageData = heartService.page(page, lambda);
        return Result.toSuccess(new PageData<HeartEntity>(pageData.getRecords(), pageData.getTotal()));
    }
}
