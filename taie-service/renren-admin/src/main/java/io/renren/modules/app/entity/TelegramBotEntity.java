package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("telegram_bot")
public class TelegramBotEntity extends AppBaseEntity {

	private Long chatId;
	
	private String userName;
	
	private String pkg;
	
	private String proxyName;
	
}
