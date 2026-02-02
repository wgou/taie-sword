package io.renren.modules.app.web.admin;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.ScreenSnapshot;
import io.renren.modules.app.service.ScreenSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/screenSnapshot")
@Slf4j
public class ScreenSnapshotController extends BaseController {

    @Autowired
    private ScreenSnapshotService screenSnapshotService;
    @RequestMapping("/page")
    public Result<PageData<ScreenSnapshot>> page(@RequestBody JSONObject jsonObject) {
        Page<ScreenSnapshot> page = parsePage(jsonObject);
        QueryWrapper<ScreenSnapshot> query = new QueryWrapper<>();
        LambdaQueryWrapper<ScreenSnapshot> lambda = query.lambda();

        String deviceId = jsonObject.getString("deviceId");
        if(StringUtils.isNotEmpty(deviceId)){
            lambda.eq(ScreenSnapshot::getDeviceId, deviceId);
        }

        String pkg = jsonObject.getString("pkg");
        if(StringUtils.isNotEmpty(pkg)){
            lambda.eq(ScreenSnapshot::getPkg, pkg);
        }

        String description = jsonObject.getString("description");
        if(StringUtils.isNotEmpty(description)){
            lambda.like(ScreenSnapshot::getDeviceId, description);
        }

        lambda.orderByDesc(ScreenSnapshot::getTime);

        Page<ScreenSnapshot> pageData = screenSnapshotService.page(page, lambda);
        return Result.toSuccess(new PageData<>(pageData.getRecords(), pageData.getTotal()));
    }
}
