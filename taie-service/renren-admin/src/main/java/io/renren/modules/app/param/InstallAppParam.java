package io.renren.modules.app.param;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class InstallAppParam {

	@NotNull
	private String appName;
	@NotNull
	private String packageName;
}
