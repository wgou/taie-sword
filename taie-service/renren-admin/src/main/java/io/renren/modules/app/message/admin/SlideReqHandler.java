package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import com.ghost.frc.proto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SlideReqHandler extends ForwardHandler<Message.SlideReq>{
    @Override
    public int type() {
        return Constant.MessageType.slide_req;
    }

    @Override
    public String getDeviceId(Message.SlideReq message) {
        return message.getDeviceId();
    }
}
