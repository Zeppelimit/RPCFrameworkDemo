package com.li.registry;

import com.li.common.URL;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Data
public class ZookeeperClient {

    private final CuratorFramework client;

    public static Map<String,ZookeeperClient> zookeeperConnSet = new ConcurrentHashMap<>();

    private List<String> registerServiceList = new ArrayList<>();

    private TreeCache cache;

    public ZookeeperClient(String zkAddress) {
        try {
            // 设置zk临时节点的有效时间sessionTimeout，连接时间和会话超时时间是不一样的
            int timeout = 3000;
            int retryTimeMillis = 1000;
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(zkAddress)
                    .retryPolicy(new RetryNTimes(3, retryTimeMillis))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(timeout);
            client = builder.build();
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    if (state == ConnectionState.LOST) {
                        log.info("zk断开");
                        //   CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
                    } else if (state == ConnectionState.CONNECTED) {
                        log.info("zk连接成功");
                        //     CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                    } else if (state == ConnectionState.RECONNECTED) {
                       log.info("zk重新连接");
                        //  这里有状态，重新连接时，这里需要重新注册关注的事件

                        //   CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                    }
                }
            });
            client.start();

            zookeeperConnSet.put(zkAddress,this);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void initLocalCache(String watchRootPath) throws Exception {

        CuratorCache cache = CuratorCache.build(client, watchRootPath);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node -> log.info("Node created: {}", node.getPath()))
                .forChanges((oldNode, node) -> log.info("Node changed. Old: {} New: {}", oldNode.getPath(), node.getPath()))
                .forDeletes(oldNode -> log.info("Node deleted. Old value: {}", oldNode.getPath()))
                .forInitialized(() -> log.info("Cache initialized"))
                .build();

        // register the listener
        cache.listenable().addListener(listener);

        // the cache must be started
        cache.start();

    }


    public void deleteNode(final String path) {
        try {
            deleteNode(path,true);
        } catch (Exception ex) {
            log.error("{}",ex);
        }
    }

    public void deleteNode(final String path,Boolean deleteChildren){
        try {
            if(deleteChildren){
                //guaranteed()删除一个节点，强制保证删除,
                // 只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到删除节点成功
                client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            }else{
                client.delete().guaranteed().forPath(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doClose() {
        log.info("关闭zk");
        client.close();
    }

    public List<String> getNode(String path) throws Exception {
        List<String> nodeList;
        if(cache != null){
            log.info("从cache中获取");
            Map<String, ChildData> currentChildren = cache.getCurrentChildren(path);
            nodeList = new ArrayList<>(currentChildren.keySet());
        }
        else{
            GetChildrenBuilder children = client.getChildren();
            nodeList = children.forPath(path);
        }

        return nodeList;
    }



    public void createNode(CreateMode mode, String path , String nodeData) {
        try {
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().withMode(mode).forPath(path,nodeData.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("注册出错", e);
        }
    }

    public void createNode(CreateMode mode,String path) {
        try {
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
        } catch (Exception e) {
            log.error("注册出错", e);
        }
    }

    public void setNodeData(String path, byte[] data){
        try {
            client.setData().forPath(path, data);
        }catch (Exception ex) {
            log.error("{}",ex);
        }
    }

    public byte[] getNodeData(String path){
        Byte[] bytes = null;
        try {
            client.getData().forPath(path);
            return client.getData().forPath(path);
        }catch (Exception ex) {
            log.error("{}",ex);
        }
        return null;
    }

    public boolean isExistNode(final String path) {
        client.sync();
        try {
            return null != client.checkExists().forPath(path);
        } catch (Exception ex) {
            return false;
        }
    }

    ExecutorService pool = Executors.newFixedThreadPool(5);

    public List<URL> watchPath(String watchPath,NotifyListener notifyListener) throws Exception {
        TreeCache treeCache = new TreeCache(client, watchPath);

        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                List<URL> urls = toUrls(getNode(watchPath));
                if(urls != null){
                    notifyListener.notify(urls);
                }
            }
        });

        List<URL> urls = toUrls(getNode(watchPath));

        return urls;
    }

    public InterProcessReadWriteLock getReadWriteLock(String path){
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, path);
        return readWriteLock;
    }

    public static ZookeeperClient getRegistryClient(URL url) {
        String address = url.getHost() + ":" + url.getPort();
        Map<String, ZookeeperClient> zookeeperConnSet = ZookeeperClient.zookeeperConnSet;
        ZookeeperClient zookeeperClient = zookeeperConnSet.getOrDefault(address, null);
        if(zookeeperClient == null){
            zookeeperClient = new ZookeeperClient(address);
            zookeeperConnSet.put(address,zookeeperClient);
        }
        return zookeeperClient;
    }

    private List<URL> toUrls(List<String> nodes){
       List<URL> urls = new ArrayList<>();
       for(String s : nodes){
           urls.add(URL.getUrl(s));
       }
       return urls;
    }

}
