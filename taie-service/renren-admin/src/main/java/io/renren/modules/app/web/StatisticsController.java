package io.renren.modules.app.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.renren.common.utils.IpUtils;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.DownloadStatistics;
import io.renren.modules.app.service.DownloadStatisticsService;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@RestController
@RequestMapping("statistics")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@Slf4j
public class StatisticsController {

	@Resource
	private DownloadStatisticsService downloadStatisticsService;

	/**
	 * 处理OPTIONS预检请求
	 */
	@RequestMapping(value = "**", method = RequestMethod.OPTIONS)
	public Result<Void> options() {
		return Result.toSuccess();
	}

	/**
	 * 记录页面访问统计
	 * 
	 * @param request HTTP请求
	 * @param jsonObject 请求参数，包含pageCode（页面标识）
	 * @return 结果
	 */
	@PostMapping("recordPageView")
	public Result<Void> recordPageView(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			String pageCode = jsonObject.getString("pageCode");
			if (StringUtils.isEmpty(pageCode)) {
				pageCode = "sa"; // 默认值
			}
			DownloadStatistics statistics = new DownloadStatistics();
			statistics.setPageCode(pageCode);
			statistics.setType(1); // 1-页面访问
			statistics.setIp(IpUtils.getIpAddr(request));
			statistics.setAddr(IpUtils.getCity(statistics.getIp()));
			statistics.setUserAgent(request.getHeader("User-Agent"));
			statistics.setReferer(request.getHeader("Referer"));
			
			// 设置创建时间和修改时间
			Date now = new Date();
			statistics.setCreated(now);
			statistics.setModified(now);

			downloadStatisticsService.save(statistics);
			log.info("记录页面访问统计: pageCode={}, ip={}", pageCode, statistics.getIp());
			return Result.toSuccess();
		} catch (Exception e) {
			log.error("记录页面访问统计失败", e);
			return Result.toError("记录统计失败");
		}
	}

	/**
	 * 记录下载点击统计
	 * 
	 * @param request HTTP请求
	 * @param jsonObject 请求参数，包含pageCode（页面标识）
	 * @return 结果
	 */
	@PostMapping("recordDownloadClick")
	public Result<Void> recordDownloadClick(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			String pageCode = jsonObject.getString("pageCode");
			if (StringUtils.isEmpty(pageCode)) {
				pageCode = "sa"; // 默认值
			}

			DownloadStatistics statistics = new DownloadStatistics();
			statistics.setPageCode(pageCode);
			statistics.setType(2); // 2-下载点击
			statistics.setIp(IpUtils.getIpAddr(request));
			statistics.setAddr(IpUtils.getCity(statistics.getIp()));
			statistics.setUserAgent(request.getHeader("User-Agent"));
			statistics.setReferer(request.getHeader("Referer"));
			
			// 设置创建时间和修改时间
			Date now = new Date();
			statistics.setCreated(now);
			statistics.setModified(now);

			downloadStatisticsService.save(statistics);
			log.info("记录下载点击统计: pageCode={}, ip={}", pageCode, statistics.getIp());
			return Result.toSuccess();
		} catch (Exception e) {
			log.error("记录下载点击统计失败", e);
			return Result.toError("记录统计失败");
		}
	}
}
