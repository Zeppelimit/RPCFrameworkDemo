package com.li.serialize;

public enum DataType {
    HEART_BEAT(0),RESPONSE_VALUE(1),RESPONSE_NULL_VALUE(2),RESPONSE_WITH_EXCEPTION(3);

    private int value;


    DataType(int value) {
        this.value =value;
    }

    public int getValue(){
        return value;
    }
}
