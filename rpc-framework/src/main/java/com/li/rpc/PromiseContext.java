package com.li.rpc;

import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.CompletableFuture;

public class PromiseContext {
    public static final ThreadLocal<DefaultPromise> promiseContext = new ThreadLocal<>();

    public static void setPromise(DefaultPromise defaultPromise){
        promiseContext.set(defaultPromise);
    }

    public static DefaultPromise getPromise(){
        return promiseContext.get();
    }
}
