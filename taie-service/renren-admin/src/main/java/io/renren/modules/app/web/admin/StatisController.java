package io.renren.modules.app.web.admin;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.DownloadStatistics;
import io.renren.modules.app.param.StatisticsParam;
import io.renren.modules.app.service.DownloadStatisticsService;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("statis")
@Slf4j
public class StatisController {

	@Resource
	private DownloadStatisticsService downloadStatisticsService;


    @RequestMapping("page")
    public Result<PageData<DownloadStatistics>> page(@RequestBody StatisticsParam param) {
        Page<DownloadStatistics> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<DownloadStatistics> query = new QueryWrapper<>();
        LambdaQueryWrapper<DownloadStatistics> lambda = query.lambda();

        // 统计类型筛选
        if (param.getType() != null) {
            lambda.eq(DownloadStatistics::getType, param.getType());
        }
        
        // 页面标识筛选（如果参数中有pageCode字段）
        // 注意：需要在StatisticsParam中添加pageCode字段才能使用
        // if (StringUtils.isNotEmpty(param.getPageCode())) {
        //     lambda.eq(DownloadStatistics::getPageCode, param.getPageCode());
        // }
         
        // 时间范围筛选
        if (param.getStart() != null) {
            lambda.ge(DownloadStatistics::getCreated, param.getStart());
        }

        if (param.getEnd() != null) {
            lambda.le(DownloadStatistics::getCreated, param.getEnd());
        }
 
        lambda.orderByDesc(DownloadStatistics::getCreated);
        Page<DownloadStatistics> pageData = downloadStatisticsService.page(page, lambda);
        return Result.toSuccess(new PageData<DownloadStatistics>(pageData.getRecords(), pageData.getTotal()));
    }
}
