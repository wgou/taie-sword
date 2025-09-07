/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.config;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.renren.common.exception.RenException;
import io.renren.modules.app.context.DeviceContext;
import lombok.extern.slf4j.Slf4j;
/**
 */
@Slf4j
@Component
public class ApiInterceptor implements HandlerInterceptor {

    public static final String _deviceNo_ = "deviceNo";
     
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String deviceNo = request.getHeader(_deviceNo_);
    	if(StringUtils.isEmpty(deviceNo)) throw new RenException(401,"Invalid deviceNo");
    	DeviceContext.setDeviceId(deviceNo);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    		throws Exception {
    	DeviceContext.clear();
    }
}
