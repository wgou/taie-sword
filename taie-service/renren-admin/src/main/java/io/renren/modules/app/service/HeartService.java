package io.renren.modules.app.service;

import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.HeartEntity;

public interface HeartService extends IService<HeartEntity> {
	
	
	void addHeart(String pkg,String deviceId);
	
	
}
