package io.renren;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class SocketIoTest {
    public static void main(String[] args) {
        SocketIOServer socketIOServer = null;
        socketIOServer.addEventListener("", byte[].class, (SocketIOClient client, byte[] data, AckRequest ack) -> {
        });
    }
}
