package io.renren.modules.app.message;

/**
 * ADMIN 向设备 发送执行指令 
 * 
 * 设备发出消息
 * 
 * @author Administrator
 * @param <T>
 */
public interface MessageHandler<T> {
    void handler(T message, Context context) throws Exception;

    int source();
    
    int type();

}
