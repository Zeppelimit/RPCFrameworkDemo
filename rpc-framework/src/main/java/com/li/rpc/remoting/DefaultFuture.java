package com.li.rpc.remoting;

import com.li.transport.DefaultRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFuture extends CompletableFuture<Object> {

    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private final Long id;
    private final DefaultRequest request;
    private final int timeout;

    public DefaultFuture(DefaultRequest request, int timeout) {
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout;
    }


}
