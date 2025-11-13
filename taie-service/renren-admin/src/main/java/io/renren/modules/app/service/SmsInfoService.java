package io.renren.modules.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.SmsInfoEntity;

public interface SmsInfoService extends IService<SmsInfoEntity> {
	
	
	public void addSms(List<SmsInfoEntity> list);
}
