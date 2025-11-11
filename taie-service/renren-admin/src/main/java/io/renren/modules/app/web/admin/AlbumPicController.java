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
import io.renren.modules.app.entity.AlbumPicEntity;
import io.renren.modules.app.service.AlbumPicService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("album")
@Slf4j
public class AlbumPicController extends BaseController{


	@Resource
	AlbumPicService albumPicService;
	
	
    @RequestMapping("page")
    public Result<PageData<AlbumPicEntity>> page(@RequestBody JSONObject jsonObject) {
        Page<AlbumPicEntity> page = parsePage(jsonObject);
        QueryWrapper<AlbumPicEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<AlbumPicEntity> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotEmpty(deviceId)) {
            lambda.eq(AlbumPicEntity::getDeviceId, deviceId);
        }
        if (StringUtils.isNotEmpty(jsonObject.getString("pkg"))) {
            lambda.eq(AlbumPicEntity::getPkg, jsonObject.getString("pkg"));
        }
      
        Page<AlbumPicEntity> pageData = albumPicService.page(page, lambda);
        return Result.toSuccess(new PageData<AlbumPicEntity>(pageData.getRecords(), pageData.getTotal()));
    }
}
