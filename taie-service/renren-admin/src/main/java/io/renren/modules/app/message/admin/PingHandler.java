package io.renren.modules.app.message.admin;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import io.renren.common.constant.Constant;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import com.ghost.frc.proto.Message;
import io.renren.modules.app.service.DeviceService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PingHandler implements MessageHandler<Message.Ping> {

    @Resource
    private DeviceService deviceService;
    @Override
    public void handler(Message.Ping message, Context context) throws Exception {
        WebSocketSession current = context.getCurrent();
        Message.Pong.Builder builder = Message.Pong.newBuilder();
        builder.setDeviceId(message.getDeviceId());
        builder.setTime(System.currentTimeMillis());
        current.sendMessage(Utils.encode(Constant.MessageType.pong, builder.build()));

//        Device device = deviceService.findByDeviceId(message.getDeviceId());
//        Device updateDevice = new Device();
//        updateDevice.setId(device.getId());
//        updateDevice.setStatus(message.getStatus());
//        deviceService.updateById(updateDevice);

    }

    @Override
    public int source() {
        return Constant.MessageSource.android;
    }
    
    @Override
    public int type() {
        return Constant.MessageType.ping;
    }
}
