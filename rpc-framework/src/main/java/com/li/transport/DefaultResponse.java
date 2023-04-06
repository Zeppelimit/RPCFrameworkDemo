package com.li.transport;

import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import io.protostuff.Tag;
import lombok.Data;

@Data
public class DefaultResponse {
    @Tag(1)
    private int stateCode;
    @Tag(2)
    private long id;
    @Tag(3)
    private AppRpcResult retData;
}
