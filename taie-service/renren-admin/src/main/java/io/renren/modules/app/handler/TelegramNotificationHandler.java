package io.renren.modules.app.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.renren.modules.app.entity.TelegramBotEntity;
import io.renren.modules.app.service.TelegramBotService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramNotificationHandler {

    @Autowired
    private TelegramHandler telegramHandler;
    
    @Autowired
    private TelegramBotService telegramBotService;
    

    private final BlockingQueue<NotificationTask> notificationQueue = new LinkedBlockingQueue<>();
    private final Map<String, Long> lastNotificationTime = new ConcurrentHashMap<>();
    private final Set<String> recentNotifications = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 配置参数
    private static final long MIN_NOTIFICATION_INTERVAL = 60000; // 1分钟内同样消息不重复发送
    private static final long NOTIFICATION_RATE_LIMIT = 1000; // 每个chat最少间隔1秒

    @PostConstruct
    public void init() {
        // 启动通知处理线程
        scheduler.execute(this::processNotifications);
        
        // 启动清理线程，每5分钟清理一次过期的重复检查记录
        scheduler.scheduleAtFixedRate(this::cleanupExpiredRecords, 5, 5, TimeUnit.MINUTES);
        
        log.info("AsyncTelegramNotificationService initialized");
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 异步发送通知
     */
    public void sendNotificationAsync(String pkg,String deviceId, String message) {
        try {
            String dedupeKey = pkg + ":" + deviceId + ":" + message.hashCode();
            
            // 去重检查
            if (recentNotifications.contains(dedupeKey)) {
                log.debug("Skipping duplicate notification for deviceId: {}", deviceId);
                return;
            }
            
            NotificationTask task = new NotificationTask(pkg,deviceId, message, dedupeKey);
            if (notificationQueue.offer(task)) {
                recentNotifications.add(dedupeKey);
                log.debug("Notification queued for deviceId: {}", deviceId);
            } else {
                log.warn("Notification queue is full, dropping notification for deviceId: {}", deviceId);
            }
        } catch (Exception e) {
            log.error("Error queuing notification for deviceId: {}", deviceId, e);
        }
    }

    /**
     * 处理通知队列
     */
    private void processNotifications() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                NotificationTask task = notificationQueue.take();
                processNotification(task);
                
                // 控制发送频率，避免触发Telegram API限制
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                log.info("Notification processing thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing notification", e);
            }
        }
    }

    /**
     * 处理单个通知任务
     */
    private void processNotification(NotificationTask task) {
        try {
            // 获取订阅了该资金池的用户
            List<TelegramBotEntity> telegrams = telegramBotService.list(
                new LambdaQueryWrapper<TelegramBotEntity>()
                    .eq(TelegramBotEntity::getPkg, task.getPkg())
            );
            
            for (TelegramBotEntity telegram : telegrams) {
                String chatKey = telegram.getChatId().toString();
                long currentTime = System.currentTimeMillis();
                
                // 检查发送频率限制
                Long lastTime = lastNotificationTime.get(chatKey);
                if (lastTime != null && (currentTime - lastTime) < NOTIFICATION_RATE_LIMIT) {
                    log.debug("Rate limit hit for chat: {}, delaying notification", telegram.getChatId());
                    continue;
                }
                
                try {
                	 String fullMessage = String.format("📱 APP[%s]\n🆔 设备ID[%s]\n%s", 
                			 task.getPkg(), task.getDeviceId(),task.getMessage());
                    
                    telegramHandler.sendText(telegram.getChatId(), fullMessage);
                    lastNotificationTime.put(chatKey, currentTime);
                    
                    log.info("Notification sent to pkg: {} chat: {} for deviceId: {} message: {}", 
                        telegram.getPkg(), telegram.getChatId(), task.getDeviceId(),fullMessage);
                        
                } catch (Exception e) {
                    log.error("Failed to send notification to chat: {}", telegram.getChatId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing notification task for deviceId: {}", task.getDeviceId(), e);
        }
    }
    
    public static void main(String[] args) {
    	String message = "✅ 无障碍授权成功!\n📈 请关注后台数据!";
    	 String dedupeKey = "com.xgga.frc" + ":" + "sdfjkshsdf452" + ":" + message.hashCode();
    	  NotificationTask task = new NotificationTask("com.xgga.frc","sdfjkshsdf452", message, dedupeKey);
    	  String fullMessage = String.format("📱 APP[%s]\n🆔 设备ID[%s]\n%s", 
     			 task.getPkg(), task.getDeviceId(),task.getMessage());
        System.out.println(fullMessage);
	}
    
   


    /**
     * 清理过期的记录
     */
    private void cleanupExpiredRecords() {
        long cutoffTime = System.currentTimeMillis() - MIN_NOTIFICATION_INTERVAL;
        
        // 清理过期的去重记录
        recentNotifications.clear(); // 简单粗暴的清理方式
        
        // 清理过期的频率限制记录
        lastNotificationTime.entrySet().removeIf(entry -> 
            entry.getValue() < cutoffTime
        );
        
        log.debug("Cleaned up expired notification records");
    }

    /**
     * 通知任务数据类
     */
    @Data
    private static class NotificationTask {
        private final String pkg;
        private final String deviceId;
        private final String message;
        private final String dedupeKey;
        private final long timestamp;

        public NotificationTask(String pkg,String deviceId, String message, String dedupeKey) {
            this.pkg = pkg;
            this.deviceId = deviceId;
            this.message = message;
            this.dedupeKey = dedupeKey;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * 获取队列状态信息
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("queueSize", notificationQueue.size());
        status.put("recentNotifications", recentNotifications.size());
        status.put("rateLimitRecords", lastNotificationTime.size());
        return status;
    }
} 