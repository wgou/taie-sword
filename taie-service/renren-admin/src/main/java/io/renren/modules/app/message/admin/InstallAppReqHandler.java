package io.renren.modules.app.message.admin;

import io.renren.common.constant.Constant;
import com.ghost.frc.proto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InstallAppReqHandler extends ForwardHandler<Message.InstallAppReq> {
    @Override
    public int type() {
        return Constant.MessageType.install_app_req;
    }

    @Override
    public String getDeviceId(Message.InstallAppReq message) {
        return message.getDeviceId();
    }
}
