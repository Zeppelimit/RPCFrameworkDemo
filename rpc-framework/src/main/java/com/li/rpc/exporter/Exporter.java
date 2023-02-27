package com.li.rpc.exporter;


import com.li.rpc.invoker.Invoker;

public interface Exporter<T> {

    Invoker<T> getInvoker();

    void unExport();
}
