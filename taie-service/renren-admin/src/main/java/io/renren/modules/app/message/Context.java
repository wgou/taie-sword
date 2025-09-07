package io.renren.modules.app.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class Context {

    private ConcurrentHashMap<String, WebSocketSession> deviceMap;
    private ConcurrentHashMap<String, List<WebSocketSession>> monitorMap;//TODO 使用线程安全的list

    private WebSocketSession current;

    public WebSocketSession getDevice(String deviceId) {
        return deviceMap.get(deviceId);
    }

    public List<WebSocketSession> getMonitor(String deviceId) {
        return monitorMap.get(deviceId);
    }

    public void addMonitor(String deviceId, WebSocketSession webSocketSession){
        List<WebSocketSession> webSocketSessions = monitorMap.computeIfAbsent(deviceId, k -> new Vector<>());
        webSocketSessions.add(webSocketSession);
    }

}
