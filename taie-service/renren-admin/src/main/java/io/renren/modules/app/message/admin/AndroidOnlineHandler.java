package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import com.ghost.frc.proto.Message;
import io.renren.modules.app.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Component
@Slf4j
public class AndroidOnlineHandler implements MessageHandler<Message.AndroidOnline> {

    @Resource
    private DeviceService deviceService;

    @Override
    public void handler(Message.AndroidOnline message, Context context) throws Exception {
        WebSocketSession current = context.getCurrent();
        String deviceId = message.getDeviceId();
        WebSocketSession oldDevice = context.getDevice(deviceId);
        if (oldDevice != null && oldDevice.isOpen()) {
            String oldId = oldDevice.getId();
            String id = current.getId();
            log.info("关闭老的链接:{} - {}", oldId, id);
            oldDevice.close();
        }
        context.getDeviceMap().put(deviceId, current);
    }


    @Override
    public int source() {
        return Constant.MessageSource.android;
    }

    @Override
    public int type() {
        return Constant.MessageType.android_online;
    }
}
