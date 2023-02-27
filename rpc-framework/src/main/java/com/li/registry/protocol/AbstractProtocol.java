package com.li.registry.protocol;

import com.li.common.URL;
import org.apache.zookeeper.KeeperException;
import com.li.rpc.exporter.Exporter;
import com.li.rpc.invoker.Invoker;

public abstract class AbstractProtocol implements Protocol{

    @Override
    public <T> Exporter<T> export(URL registry, Invoker<T> invoker) throws KeeperException {
        return null;
    }

    @Override
    public  <T> Invoker<T> refer(Class<T> type, URL url) throws Exception{
        return null;
    }

    @Override
    public void destroy(){

    }

}
