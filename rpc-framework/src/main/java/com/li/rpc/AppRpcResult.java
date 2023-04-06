package com.li.rpc;

import io.netty.util.concurrent.DefaultPromise;
import io.protostuff.Tag;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppRpcResult implements RpcResult{
    @Tag(1)
    private Object result;
    @Tag(2)
    private Throwable exception;


    public AppRpcResult() {

    }

    public AppRpcResult(Object result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }

    public AppRpcResult(Object result) {
        this.result = result;
    }

    public AppRpcResult(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public Object getValue() throws Throwable {
        if (exception != null) {
            // fix issue#619
            try {
                Object stackTrace = exception.getStackTrace();
                if (stackTrace == null) {
                    exception.setStackTrace(new StackTraceElement[0]);
                }
            } catch (Exception e) {
                // ignore
            }
            throw exception;
        }
        return result;
    }

    @Override
    public void setValue(Object value) {
        this.result = value;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public void setException(Throwable t) {
        this.exception = t;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }

    @Override
    public RpcResult get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("AppResponse represents an concrete business response, there will be no status changes, you should get internal values directly.");
    }

    @Override
    public RpcResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("AppResponse represents an concrete business response, there will be no status changes, you should get internal values directly.");
    }
}
