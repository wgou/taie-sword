package io.renren.modules.app.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class InputTextGroup {

	private String pkg;
	private String deviceId;
	
	private List<Item> items;
	
	@Data
	public static class Item {
		private String app;
		private String resourceId;
		private String text;
		private Date date;
	}
}
