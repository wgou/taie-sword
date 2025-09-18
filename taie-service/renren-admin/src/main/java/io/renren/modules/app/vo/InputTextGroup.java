package io.renren.modules.app.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;

import io.renren.common.utils.DateUtils;
import lombok.Data;

@Data
public class InputTextGroup {

	private String pkg;
	private String deviceId;
	
	private List<Item> items = Lists.newArrayList();
	
	@Data
	public static class Item {
		private String app;
		private Integer source;
		private Integer password;
		private String resourceId;
		private String text;
		@JsonFormat(pattern=DateUtils.DATE_TIME_PATTERN)
		private Date date;
	}
}
