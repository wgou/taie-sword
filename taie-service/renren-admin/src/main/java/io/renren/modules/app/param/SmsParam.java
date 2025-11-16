package io.renren.modules.app.param;

import lombok.Data;

@Data
public class SmsParam extends PkgParam{

	private String deviceId;
	
	private String content;
}
