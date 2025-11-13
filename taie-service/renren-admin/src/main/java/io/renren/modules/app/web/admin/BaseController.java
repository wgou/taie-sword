package io.renren.modules.app.web.admin;


import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
@RestController
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
    
 
 
}
