package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
@TableName("sms_info")
@Data
public class SmsEntity extends AppBaseEntity{

	private String deviceId;
	
	private String pkg;
	
	private String content;
	
	
}
