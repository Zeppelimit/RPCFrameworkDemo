package com.li.serialize.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class RpcHeader {
    private final int MAGIC = 0xCAFE;

    private byte messageType;

    private byte isEvent;

    private byte status;

    private byte serializeMethod;

    private long id;

}
