package io.renren;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import io.renren.common.utils.HttpUtils;

public class Main {
    public static void main(String[] args) {
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
