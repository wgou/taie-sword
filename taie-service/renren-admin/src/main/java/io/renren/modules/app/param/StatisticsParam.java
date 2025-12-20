package io.renren.modules.app.param;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.renren.common.utils.DateUtils;
import lombok.Data;

@Data
public class StatisticsParam extends  PageParam{

	
	private Integer type;
	
	 @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date start ;
	
	 @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
	private Date end ;
	
}
