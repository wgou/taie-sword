package io.renren.modules.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WakeStatusEnum {

	unWake(0,"未唤醒"),
	wakeIng(1,"等待唤醒"),
	waked(2,"已唤醒");

	private Integer code;
	private String desc;
}
