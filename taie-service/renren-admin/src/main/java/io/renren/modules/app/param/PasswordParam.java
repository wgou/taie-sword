package io.renren.modules.app.param;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class PasswordParam {

	@NotNull
	private String packageName;
	@NotNull
	private JSONObject password;
	
}
