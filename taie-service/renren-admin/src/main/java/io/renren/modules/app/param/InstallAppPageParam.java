package io.renren.modules.app.param;

import lombok.Data;

@Data
public class InstallAppPageParam extends PkgParam	{

	private String deviceId;
	
	private String packageName;
	
	private String remark ;
}
