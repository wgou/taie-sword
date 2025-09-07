package io.renren.modules.app.message.admin;

import org.springframework.stereotype.Component;

import io.renren.common.constant.Constant;
import io.renren.modules.app.message.proto.Message;

@Component
public class RecentsReqHandler extends ForwardHandler<Message.RecentsReq> {


    @Override
    public String getDeviceId(Message.RecentsReq message) {
        return message.getDeviceId();
    }
    
    @Override
    public int type() {
        return Constant.MessageType.recents_req;
    }
}
