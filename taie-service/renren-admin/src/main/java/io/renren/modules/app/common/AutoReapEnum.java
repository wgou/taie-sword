package io.renren.modules.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AutoReapEnum {

	NotReap(0,"不自动收割"),
	Reap(1,"自动收割");
	

	private Integer code;
	private String desc;
}
