package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import com.ghost.frc.proto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScreenReqHandler extends ForwardHandler<Message.ScreenReq> {
    @Override
    public int type() {
        return Constant.MessageType.screen_req;
    }

    @Override
    public String getDeviceId(Message.ScreenReq message) {
        return message.getDeviceId();
    }
}
