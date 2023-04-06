package com.li.transport.client.struct;

import io.protostuff.Tag;
import lombok.Data;

@Data
public class RpcMessage {
    @Tag(1)
    private RpcHeader rpcHeader;
    @Tag(2)
    private Object data;

}
