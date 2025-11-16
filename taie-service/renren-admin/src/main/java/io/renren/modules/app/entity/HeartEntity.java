package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("ast_heart")
public class HeartEntity extends AppBaseEntity{
	
	private String deviceId;
	
	private String pkg;

}
