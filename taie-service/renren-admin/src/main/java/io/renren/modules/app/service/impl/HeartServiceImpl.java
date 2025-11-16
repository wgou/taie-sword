package io.renren.modules.app.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.HeartEntity;
import io.renren.modules.app.mapper.HeartMapper;
import io.renren.modules.app.service.HeartService;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class HeartServiceImpl extends ServiceImpl<HeartMapper, HeartEntity> implements HeartService {
	
	
	@Override
	public void addHeart(String pkg, String deviceId) {
		HeartEntity heart = new HeartEntity();
		heart.setPkg(pkg);
		heart.setDeviceId(deviceId);
		this.save(heart);
	}
	
}
