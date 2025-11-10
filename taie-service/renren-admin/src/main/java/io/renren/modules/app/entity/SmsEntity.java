package io.renren.modules.app.entity;

import lombok.Data;

@Data
public class SmsEntity extends AppBaseEntity{

	private String deviceId;
	
	private String pkg;
	
	private String content;
	
	
}
