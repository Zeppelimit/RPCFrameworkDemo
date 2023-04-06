package com.li.transport;

import io.protostuff.Tag;
import lombok.Data;
import com.li.rpc.invoker.RpcInvocation;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class DefaultRequest {
    @Tag(1)
    private static AtomicLong idGenerator = new AtomicLong();
    @Tag(2)
    private RpcInvocation invocation;
    @Tag(3)
    private long id = idGenerator.incrementAndGet();
}
