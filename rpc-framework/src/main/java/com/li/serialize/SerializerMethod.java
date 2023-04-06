package com.li.serialize;

public enum SerializerMethod {
    Json(0),Binary(1),Protostuff(2),Kryo(3);

    private int value;
    SerializerMethod(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
