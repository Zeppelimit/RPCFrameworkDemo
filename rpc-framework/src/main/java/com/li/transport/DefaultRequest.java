package com.li.transport;

import lombok.Data;
import com.li.rpc.invoker.RpcInvocation;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class DefaultRequest {

    private static AtomicLong idGenerator = new AtomicLong();

    private RpcInvocation invocation;

    private long id = idGenerator.incrementAndGet();
}
