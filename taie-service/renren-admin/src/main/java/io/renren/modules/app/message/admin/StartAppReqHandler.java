package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import com.ghost.frc.proto.Message;
import org.springframework.stereotype.Component;

@Component
public class StartAppReqHandler extends ForwardHandler<Message.StartAppReq>{
    @Override
    public int type() {
        return Constant.MessageType.start_app_req;
    }

    @Override
    public String getDeviceId(Message.StartAppReq message) {
        return message.getDeviceId();
    }
}
