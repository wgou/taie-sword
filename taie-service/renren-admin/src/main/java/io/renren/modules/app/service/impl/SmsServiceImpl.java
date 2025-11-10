package io.renren.modules.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.SmsEntity;
import io.renren.modules.app.mapper.SmsMapper;
import io.renren.modules.app.service.SmsService;

@Service
public class SmsServiceImpl extends ServiceImpl<SmsMapper, SmsEntity> implements SmsService {

	
	@Override
	public void addSms(List<SmsEntity> list) {
		
		this.saveBatch(list);
	}
}
