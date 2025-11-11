package io.renren.modules.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.SmsInfoEntity;
import io.renren.modules.app.mapper.SmsInfoMapper;
import io.renren.modules.app.service.SmsInfoService;

@Service
public class SmsInfoServiceImpl extends ServiceImpl<SmsInfoMapper, SmsInfoEntity> implements SmsInfoService {

	
	@Override
	public void addSms(List<SmsInfoEntity> list) {
		
		this.saveBatch(list);
	}
}
