package io.renren;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import com.google.common.collect.Maps;

import io.renren.common.utils.HttpUtils;

public class Main {
    public static void main(String[] args) {
    	 String imagePath = "C:\\Users\\PC\\Downloads\\JdTPN00aKw.jpg"; // 图片路径
         try {
             String base64 = encodeImageToBase64(imagePath);
             System.out.println(base64); // 输出 Base64 字符串
         } catch (IOException e) {
             e.printStackTrace();
         }
    	
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
