package io.renren.modules.app.web.admin;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import io.renren.common.exception.RenException;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.MajorData;
import io.renren.modules.app.service.MajorDataService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("majorData")
@RestController
public class MajorDataController {
    @Resource
    private MajorDataService majorDataService;
    @RequestMapping("findByDeviceIdAndResourceId")
    public Result<MajorData> findByDeviceIdAndResourceId(@RequestBody JSONObject jsonObject){
        String deviceId = jsonObject.getString("deviceId");
        String resourceId= jsonObject.getString("resourceId");

        if(StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(resourceId)){
            throw new RenException("查询条件不足!");
        }
        MajorData result = majorDataService.findByDeviceIdAndResourceId(deviceId, resourceId);
        return Result.toSuccess(result);
    }
}
