package io.renren.modules.app.param;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TransferRecordParam {

	@NotNull
	private String packageName;
	
	private String from;
	
	private String to;
	
	private BigDecimal amount;
}
