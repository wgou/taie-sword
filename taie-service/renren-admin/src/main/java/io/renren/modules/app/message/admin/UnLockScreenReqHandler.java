package io.renren.modules.app.message.admin;

import org.springframework.stereotype.Component;

import io.renren.common.constant.Constant;
import io.renren.modules.app.message.proto.Message;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnLockScreenReqHandler extends ForwardHandler<Message.UnLockScreenReq>{
    @Override
    public int type() {
        return Constant.MessageType.un_lock_screen_req;
    }

    @Override
    public String getDeviceId(Message.UnLockScreenReq message) {
        return message.getDeviceId();
    }
}
