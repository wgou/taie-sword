package io.renren;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import io.renren.common.utils.HttpUtils;

public class Main {
    public static void main(String[] args) {
    	String configer = "{\r\n"
    			+ "  \"brand\": \"huawei_honor\",\r\n"
    			+ "  \"rules\": [\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"android:id/title\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"无障碍 / 设置 / 应用详情页-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"myfiles.filemanager.fileexplorer.cleaner:id/nameText\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"电话清洁器-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.anti.virus.security:id/tv_app_name\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"Super Antivirus-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.tencent.android.qqdownloader:id/a92\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"应用宝-卸载页应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.qihoo.appstore:id/uninstall_name\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"360手机助手-卸载应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.weilai.shoujiguanjia:id/appLabel\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"手机管理大师-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"cn.opda.a.phonoalbumshoushou:id/a\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"百度手机卫士-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"kaiqi.cleanmaster:id/tv_app_name\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"手机清理大师-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"android:id/alertTitle\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"系统卸载确认弹窗标题\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.cm.plugin.internal:id/asp\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"猎豹清洁大师-应用名称\" }\r\n"
    			+ "    ],\r\n"
    			+ "    [\r\n"
    			+ "      { \"id\": \"com.zs.clean:id/appname\", \"pattern\": \".*{{appName}}.*\", \"comment\": \"2345清理王-应用名称\" }\r\n"
    			+ "    ]\r\n"
    			+ "  ]\r\n"
    			+ "}\r\n"
    			+ "";
    	   JSONObject json = JSONObject.parseObject(configer);
           String backFeatures = json.getString("rules");
           System.out.println(backFeatures);
    	
    }
    
    public static String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStream.read(bytes);
        fileInputStream.close();

        // 使用 Base64 编码
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    public void test1() {

    	Map<String, String> headers = Maps.newHashMap();
    	headers.put("pkg", "com.ghost.rc");
    	headers.put("device_id", "339f5558bec48058");
    	
         try {
			String resp = new  HttpUtils().post("http://192.168.0.128:8080/ast/api/device/getConfig", 
					"{}", headers);
			System.out.println(resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    
    }
}
