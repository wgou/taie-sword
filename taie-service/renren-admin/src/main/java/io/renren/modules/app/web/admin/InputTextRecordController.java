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
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.mapper.InputTextRecordMapper;
import io.renren.modules.app.param.InputTextRecordParam;
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
    public Result<PageData<InputTextRecord>> page(@RequestBody InputTextRecordParam param) {
        Page<InputTextRecord> page = new Page<>(param.getPage(), param.getLimit());
        page.setOptimizeCountSql(false);
        QueryWrapper<InputTextRecord> query = new QueryWrapper<>();
        LambdaQueryWrapper<InputTextRecord> lambda = query.lambda();
        
        // 设备ID
        if (StringUtils.isNotEmpty(param.getDeviceId())) {
            lambda.eq(InputTextRecord::getDeviceId, param.getDeviceId());
        }
        
        // 包名
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(InputTextRecord::getPkg, param.getPkg());
        }
        
        // 应用包名
        if (StringUtils.isNotEmpty(param.getAppPkg())) {
            lambda.eq(InputTextRecord::getAppPkg, param.getAppPkg());
        }
        
        // 来源
        if (param.getSource() != null) {
            lambda.eq(InputTextRecord::getSource, param.getSource());
        }
        
        // 开始时间
        if (param.getStartTime() != null) {
            lambda.ge(InputTextRecord::getTime, param.getStartTime());
        }
        
        // 结束时间
        if (param.getEndTime() != null) {
            lambda.le(InputTextRecord::getTime, param.getEndTime());
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
    public Result<InputTextGroup> group(@RequestBody InputTextRecordParam param) {
        try {
            DynamicContextHolder.push("clickhouse");
            InputTextGroup groups = inputTextRecordService.queryGroupedRecords(param);
            return Result.toSuccess(groups);
        } finally {
            DynamicContextHolder.poll();
        }
    }

    
}
