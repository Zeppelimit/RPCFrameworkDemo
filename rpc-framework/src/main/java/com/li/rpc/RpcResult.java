package com.li.rpc;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface RpcResult extends Serializable {

    Object getValue() throws Throwable;

    void setValue(Object value);

    Throwable getException();

    void setException(Throwable t);

    boolean hasException();

    RpcResult get() throws InterruptedException, ExecutionException;

    RpcResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
