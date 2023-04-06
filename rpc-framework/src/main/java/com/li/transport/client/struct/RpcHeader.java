package com.li.transport.client.struct;

import io.protostuff.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class RpcHeader {
    @Tag(1)
    private final int MAGIC = 0xCAFE;
    @Tag(2)
    private byte messageType;
    @Tag(3)
    private byte isEvent;
    @Tag(4)
    private byte status;
    @Tag(5)
    private byte serializeMethod;
    @Tag(6)
    private long id;

}
