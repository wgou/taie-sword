package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import com.ghost.frc.proto.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class BackReqHandler implements MessageHandler<Message.BackReq> {
    @Override
    public void handler(Message.BackReq message, Context context) throws Exception {
        String deviceId = message.getDeviceId();
        WebSocketSession sws = context.getDevice(deviceId);

        WebSocketSession current = context.getCurrent();
        if (sws == null || !sws.isOpen()) {
            Message.Notify.Builder builder = Message.Notify.newBuilder();
            //TODO 提示设备下线
            builder.setContent("");
            builder.setTitle("");
            builder.setType("");
            Message.Notify notify = builder.build();
            current.sendMessage(Utils.encode(Constant.MessageType.notify, notify));
        } else {
            sws.sendMessage(Utils.encode(Constant.MessageType.back_req, message));
        }
    }

    @Override
    public int source() {
    	return Constant.MessageSource.monitor;
    }
    
    @Override
    public int type() {
        return Constant.MessageType.back_req;
    }
}
