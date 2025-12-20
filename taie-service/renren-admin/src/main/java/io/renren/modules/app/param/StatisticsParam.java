package io.renren.modules.app.param;

import java.util.Date;

import lombok.Data;

@Data
public class StatisticsParam extends  PageParam{

	
	private Integer type;
	
	private Date start ;
	
	
	private Date end ;
	
}
