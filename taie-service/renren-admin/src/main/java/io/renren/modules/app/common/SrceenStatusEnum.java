package io.renren.modules.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SrceenStatusEnum {

	closeSrceen(0,"息屏"),
	openSrceen(1,"亮屏");
	
	

	private Integer code;
	private String desc;
}
