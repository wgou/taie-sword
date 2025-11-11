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
import io.renren.modules.app.entity.AlbumPicEntity;
import io.renren.modules.app.param.AlbumPicParam;
import io.renren.modules.app.service.AlbumPicService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("album")
@Slf4j
public class AlbumPicController extends BaseController{


	@Resource
	AlbumPicService albumPicService;
	
	
    @RequestMapping("page")
    public Result<PageData<AlbumPicEntity>> page(@RequestBody AlbumPicParam param) {
	    Page<AlbumPicEntity> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<AlbumPicEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<AlbumPicEntity> lambda = query.lambda();
        if (StringUtils.isNotEmpty(param.getDeviceId())) {
            lambda.eq(AlbumPicEntity::getDeviceId, param.getDeviceId());
        }
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(AlbumPicEntity::getPkg, param.getPkg());
        }
      
        Page<AlbumPicEntity> pageData = albumPicService.page(page, lambda);
        return Result.toSuccess(new PageData<AlbumPicEntity>(pageData.getRecords(), pageData.getTotal()));
    }
}
