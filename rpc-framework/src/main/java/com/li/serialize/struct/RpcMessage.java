package com.li.serialize.struct;

import lombok.Data;

@Data
public class RpcMessage {

    private RpcHeader rpcHeader;

    private Object data;

}
