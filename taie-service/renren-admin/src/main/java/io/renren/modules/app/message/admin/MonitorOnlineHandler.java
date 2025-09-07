package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import io.renren.modules.app.message.proto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控上线 需要实时查看设备
 */
@Component
@Slf4j
public class MonitorOnlineHandler implements MessageHandler<Message.MonitorOnline> {
    @Override
    public void handler(Message.MonitorOnline message, Context context) throws Exception {
        log.info("Monitor上线 观察:{}", message.getDeviceId());
        WebSocketSession current = context.getCurrent();
        String deviceId = message.getDeviceId();
        WebSocketSession sws = context.getDevice(deviceId);

        if (sws == null || !sws.isOpen()) {
            Message.Notify.Builder builder = Message.Notify.newBuilder();
            //TODO 提示设备下线
            builder.setContent("当前设备没有上线(需要唤醒后操作)");
            builder.setTitle("提示");
            builder.setType("warning");
            Message.Notify notify = builder.build();
            current.sendMessage(Utils.encode(Constant.MessageType.notify, notify));
        }
        context.addMonitor(deviceId, current);

    }

    @Override
    public int source() {
        return Constant.MessageSource.monitor;
    }
    @Override
    public int type() {
        return Constant.MessageType.monitor_online;
    }
}
