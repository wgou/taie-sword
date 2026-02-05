/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.xss;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * XSS过滤
 * @author Mark sunlightcs@gmail.com
 */
public class XssFilter implements Filter {
	
	
	
	private static final List<String> EXCLUDE_URLS = Arrays.asList(
			"/api/device/uploadLog"
			);

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
				(HttpServletRequest) request);

		// 判断是否需要排除XSS过滤
		if (isExcludeUrl(xssRequest)) {
			chain.doFilter(request, response);
			return;
		}
		
		chain.doFilter(xssRequest, response);
	}

	
	/**
	 * 判断是否排除XSS过滤
	 */
	private boolean isExcludeUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		// 去除context path
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty()) {
			uri = uri.substring(contextPath.length());
		}
		
		for (String excludeUrl : EXCLUDE_URLS) {
			if (uri.startsWith(excludeUrl)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void destroy() {
	}

}