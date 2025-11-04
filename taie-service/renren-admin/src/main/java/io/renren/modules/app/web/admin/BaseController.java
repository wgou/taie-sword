package io.renren.modules.app.web.admin;


import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.modules.app.entity.Device;
import io.renren.modules.app.service.DeviceService;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;
@RestController
public abstract class BaseController {


    @Resource
    private DeviceService deviceService;

    public <T> Page<T> parsePage(JSONObject jsonObject) {

        long page = jsonObject.getLongValue("page");
        if (page == 0) {
            page = 1;
        }
        long limit = jsonObject.getLongValue("limit");
        if (limit == 0) {
            limit = 10;
        }
        return new Page<T>(page, limit);
    }
    
    
    public String getUser() {
    	UserDetail user = SecurityUser.getUser();
    	if(user.getUsername().equals("admin")) {
    		return null;
    	}
    	return user.getUsername();
    }
    
    
    public List<String> getDeviceIds(){
    	  QueryWrapper<Device> query = new QueryWrapper<>();
          LambdaQueryWrapper<Device> lambda = query.lambda();
          if(getUser() !=null) {
         	 lambda.eq(Device::getUser, getUser());
         	 List<Device> lists =  deviceService.list(query);
         	 List<String> deviceIds = lists.stream().map(Device::getDeviceId).collect(Collectors.toList());
         	 return deviceIds ;
         }
         return null;
    }
    
}
