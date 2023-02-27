package com.li.registry.protocol;

import com.li.Exception.RpcException;
import com.li.common.URL;
import org.apache.zookeeper.KeeperException;
import com.li.rpc.exporter.Exporter;
import com.li.rpc.invoker.Invoker;

public interface Protocol {
    int getDefaultPort();

    <T> Exporter<T> export(URL registry, Invoker<T> invoker) throws KeeperException;

    <T> Invoker<T> refer(Class<T> type, URL url) throws Exception;

    void destroy();
}
