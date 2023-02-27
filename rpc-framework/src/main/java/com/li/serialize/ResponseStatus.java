package com.li.serialize;

public enum ResponseStatus {
    OK(20),
    CLIENT_TIMEOUT(30),
    SERVER_TIMEOUT(31),
    BAD_REQUEST(40),
    BAD_RESPONSE(50),
    SERVICE_NOT_FOUND(60),
    SERVICE_ERROR(70),
    SERVER_ERROR(80),
    CLIENT_ERROR(90);

    private int value;
    ResponseStatus(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
