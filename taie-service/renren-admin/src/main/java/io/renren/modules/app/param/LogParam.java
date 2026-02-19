package io.renren.modules.app.param;

import lombok.Data;

@Data
public class LogParam extends PkgParam{

	private String deviceId;
	
	private String content;

	private String source;
}
