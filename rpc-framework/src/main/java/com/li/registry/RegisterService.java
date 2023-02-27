package com.li.registry;

import com.li.common.URL;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

public interface RegisterService {

    void register(String providerPath) throws KeeperException;

    void unregister(String providerPath);

    void close() throws IOException;

    List<URL> subscribe(String providerPath, NotifyListener notifyListener) throws Exception;

    void unsubscribe(String providerPath);

    void register(String providerPath, String data) throws KeeperException;
}
