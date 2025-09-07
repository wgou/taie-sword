package io.renren.modules.app.param;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class AppAssetsPram {

	@NotNull
	private String packageName;
	@NotNull
	private JSONObject assets;
}
