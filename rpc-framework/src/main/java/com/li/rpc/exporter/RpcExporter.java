package com.li.rpc.exporter;

import com.li.rpc.invoker.Invoker;

public class RpcExporter<T> implements Exporter<T>{

    private final Invoker<T> invoker;

    public RpcExporter(Invoker<T> invoker) {
        this.invoker = invoker;
    }

    @Override
    public Invoker<T> getInvoker() {
        return invoker;
    }

    @Override
    public void unExport() {

    }
}
