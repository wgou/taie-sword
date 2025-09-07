package io.renren.modules.app.message.admin;

import com.google.protobuf.MessageLite;
import io.renren.common.constant.Constant;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import io.renren.modules.app.message.proto.Message;
import org.springframework.web.socket.WebSocketSession;

/**
 * 转发抽象类
 * @param <T>
 */
public abstract class ForwardHandler<T extends MessageLite> implements MessageHandler<T> {
    @Override
    public void handler(T message, Context context) throws Exception {
        String deviceId = getDeviceId(message);
        WebSocketSession sws = context.getDevice(deviceId);

        WebSocketSession current = context.getCurrent();
        if (sws == null || !sws.isOpen()) {
            Message.Notify.Builder builder = Message.Notify.newBuilder();
            builder.setContent("当前设备没有上线(需要唤醒后操作)");
            builder.setTitle("提示");
            builder.setType("warning");
            Message.Notify notify = builder.build();
            current.sendMessage(Utils.encode(Constant.MessageType.notify, notify));
        } else {
            sws.sendMessage(Utils.encode(type(), message));
        }
    }

    @Override
    public int source() {
        return Constant.MessageSource.monitor;
    }

    public abstract String getDeviceId(T message);

}
