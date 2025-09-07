package io.renren.modules.app.message.admin;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import io.renren.common.constant.Constant;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import io.renren.modules.app.message.proto.Message;
import io.renren.modules.app.service.DeviceService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LockScreenHandler implements MessageHandler<Message.LockScreen> {

    @Resource
    private DeviceService deviceService;
    @Override
    public void handler(Message.LockScreen message, Context context) throws Exception {
        Device device = deviceService.findByDeviceId(message.getDeviceId());
        if(device != null){
            Device updateDevice = new Device();
            updateDevice.setId(device.getId());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", message.getType());
            jsonObject.put("value", message.getValue());
            updateDevice.setLockScreen(jsonObject);
            deviceService.updateById(updateDevice);
            log.info("{} - 新的解锁密码更新成功:{}", message.getDeviceId(), message.getValue());
        }
    }

    @Override
    public int source() {
        return Constant.MessageSource.android;
    }

    @Override
    public int type() {
        return Constant.MessageType.lock_screen;
    }
}
