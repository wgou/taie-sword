package io.renren.modules.app.web.admin;

import java.util.List;

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
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.vo.InputTextGroup;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("inputTextRecord")
public class InputTextRecordController extends BaseController {
    @Resource
    private InputTextRecordService inputTextRecordService;


    @Resource
    private InputTextRecordMapper inputTextRecordMapper;

    @RequestMapping("page")
    public Result<PageData<InputTextRecord>> page(@RequestBody JSONObject jsonObject) {
        Page<InputTextRecord> page = parsePage(jsonObject);
        page.setOptimizeCountSql(false);
        QueryWrapper<InputTextRecord> query = new QueryWrapper<>();
        LambdaQueryWrapper<InputTextRecord> lambda = query.lambda();
        String deviceId = jsonObject.getString("deviceId");
        if (StringUtils.isNotEmpty(deviceId)) {
            lambda.eq(InputTextRecord::getDeviceId, deviceId);
        }
        query.orderByDesc("time");
        try {
            DynamicContextHolder.push("clickhouse");
            return Result.toSuccess(inputTextRecordService.page(page, query));
        } finally {
            DynamicContextHolder.poll();
        }
        
    }
 

    @RequestMapping("group")
    public Result<InputTextGroup> group(@RequestBody JSONObject jsonObject) {
        try {
            DynamicContextHolder.push("clickhouse");
            InputTextGroup groups = inputTextRecordService.queryGroupedRecords(jsonObject);
            return Result.toSuccess(groups);
        } finally {
            DynamicContextHolder.poll();
        }
    }

    
}
