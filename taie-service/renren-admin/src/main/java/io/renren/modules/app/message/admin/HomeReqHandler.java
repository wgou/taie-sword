package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import com.ghost.frc.proto.Message;
import org.springframework.stereotype.Component;

@Component
public class HomeReqHandler extends ForwardHandler<Message.HomeReq> {


    @Override
    public int type() {
        return Constant.MessageType.home_req;
    }

    @Override
    public String getDeviceId(Message.HomeReq message) {
        return message.getDeviceId();
    }
}

