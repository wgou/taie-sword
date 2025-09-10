package com.example.aslib.vo;

public class HandlerMessage {
    public HandlerMessage(int type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    private int type;
    private byte[] body;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
