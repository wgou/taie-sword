package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("proxy_info")
public class ProxyInfoEntity extends AppBaseEntity{
	
	private String proxyUser;
	
	private String pkg;
	
	private String createUser;

}
