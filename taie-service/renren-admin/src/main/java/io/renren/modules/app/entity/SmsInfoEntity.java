package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
@TableName("ast_sms_info")
@Data
public class SmsInfoEntity extends AppBaseEntity{

	private String deviceId;
	
	private String pkg;
	
	private String content;

	//发送人
	private String sender;

	//短信时间
	private long date;
	
	
}
