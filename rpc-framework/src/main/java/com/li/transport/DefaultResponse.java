package com.li.transport;

import com.li.rpc.AppRpcResult;
import com.li.rpc.RpcResult;
import lombok.Data;

@Data
public class DefaultResponse {

    private int stateCode;

    private long id;

    private AppRpcResult retData;
}
