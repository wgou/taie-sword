/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * IP地址
 * 
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
public class IpUtils {
	
	    public static InetAddress getHosts() {
	        try {
	            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
	            while (networkInterfaces.hasMoreElements()) {
	                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
	                Enumeration<InetAddress> nias = ni.getInetAddresses();
	                while (nias.hasMoreElements()) {
	                    InetAddress ia = (InetAddress) nias.nextElement();
	                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
	                        return ia;
	                    }
	                }
	            }
	        } catch (SocketException e) {
	            throw new RuntimeException("获取服务器IP地址异常.");
	        }
	        return null;
	}
	
		/**
		 * 获取IP地址
		 * 
		 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
		 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
		 */
		public static String getIpAddr(HttpServletRequest request) {
		    String ip = null;
		    try {
		        String xForwardedFor = request.getHeader("x-forwarded-for");
		        if (!StringUtils.isEmpty(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
		            // 取第一个IP（真实客户端IP）
		            ip = xForwardedFor.split(",")[0].trim();
		            return ip;
		        }
	
		        ip = request.getHeader("Proxy-Client-IP");
		        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
		            ip = request.getHeader("WL-Proxy-Client-IP");
		        }
		        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
		            ip = request.getHeader("HTTP_CLIENT_IP");
		        }
		        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
		            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		        }
		        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
		            ip = request.getRemoteAddr();
		        }
		    } catch (Exception e) {
		        log.error("IPUtils ERROR ", e);
		    }
	
		    return ip;
		}
	
	
	public static String getCity(String ip) {
		String url = String.format("https://api.ipstack.com/%s?access_key=e5ce1f287818c893ec5149386d644f1a", ip);
		try {
			String response = new HttpUtils().get( url);
			JSONObject json = new JSONObject(response);
			String country_name = json.getString("country_name");
			String region_name = json.getString("region_name");
			String city = json.getString("city");
			return country_name + "/" + ( org.apache.commons.lang3.StringUtils.isEmpty(city) ? region_name : city );
		} catch (Exception e) {
			log.error("IP:{} 获取城市信息失败.",ip);
		}
		return null;
	} 

}
