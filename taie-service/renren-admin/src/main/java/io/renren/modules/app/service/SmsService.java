package io.renren.modules.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.SmsEntity;

public interface SmsService extends IService<SmsEntity> {
	
	
	public void addSms(List<SmsEntity> list);
}
