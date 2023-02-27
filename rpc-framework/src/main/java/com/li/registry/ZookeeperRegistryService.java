package com.li.registry;

import com.li.common.URL;
import lombok.Data;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Data
public class ZookeeperRegistryService implements RegisterService{
    private ZookeeperClient zookeeperClient;

    private List<String> registerServiceList = new ArrayList<>();

    public ZookeeperRegistryService(ZookeeperClient zookeeperClient) {
        this.zookeeperClient = zookeeperClient;

    }

    @Override
    public void register(String providerPath, String data) throws KeeperException {
//        zookeeperClient.create(providerPath, true);
        zookeeperClient.createNode(CreateMode.fromFlag(1),providerPath, data);
    }

    @Override
    public void register(String providerPath) throws KeeperException {
        zookeeperClient.createNode(CreateMode.fromFlag(1),providerPath);
    }

    @Override
    public void unregister(String providerPath) {
        zookeeperClient.deleteNode(providerPath);
    }


    @Override
    public void close() throws IOException {
        zookeeperClient.doClose();
    }

    @Override
    public List<URL> subscribe(String path, NotifyListener notifyListener) throws Exception {
        return zookeeperClient.watchPath(path, notifyListener);
    }

    @Override
    public void unsubscribe(String providerPath) {

    }






}
