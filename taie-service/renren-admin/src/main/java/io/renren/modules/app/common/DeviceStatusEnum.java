package io.renren.modules.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeviceStatusEnum {

	unLine(0,"离线"),
	onLine(1,"在线");
	
	

	private Integer code;
	private String desc;
}
