package com.li.serialize;

public enum MessageType {
    HEART_BEAT(0),MESSAGE_REQUEST (1),MESSAGE_RESPONSE (2);

    private int value;


    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
