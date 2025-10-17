package io.renren.modules.app.web.admin;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;

public abstract class BaseController {


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
    
}
