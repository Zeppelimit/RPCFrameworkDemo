package com.li.transport.client;

import com.li.serialize.RpcDecoder;
import com.li.serialize.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.li.serialize.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Data
public class NettyClient {

    private String ipAndPort;
    /**
     * worker可以共用
     */
    private EventLoopGroup worker = new NioEventLoopGroup();

    private Channel socketChannel;

    // 多个client共享
    private static NettyClientHandler clientHandler = new NettyClientHandler();

    /**
     * 每个client一个独立的定时线程....
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static Map<String, NettyClient> nettyClientMap = new HashMap<>();

    public NettyClient(String ipAndPort) {
        this.ipAndPort = ipAndPort;
        connect();
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public synchronized void connect() {
        log.info("建立netty连接：{}", ipAndPort);
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(worker).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        // 注意pipeline的顺序
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcDecoder(1024*1024*5,16,4))
                                    .addLast(new RpcEncoder())
                                    .addLast(new IdleStateHandler(60,0,0))
                                    .addLast(new HeartBeatReqHandler(NettyClient.this))
                                    .addLast(clientHandler);
                        }
                    });
            // 客户端是connect
            String[] values = ipAndPort.split(":");
            //  底层转换为pipeline.connect()
            ChannelFuture channelFuture = bootstrap.connect(values[0], Integer.parseInt(values[1]))
                    .sync();

            // 建立连接时保存下来，可能有需要连接多个客户端
            this.socketChannel = channelFuture.channel();

        }
        catch (Exception e) {
            log.error("与服务端建立连接失败:{}", ipAndPort, e);
            // throw new RuntimeException("与服务端建立连接失败: " + ipAndPort, e);
        }
    }


    public void send(Object msg) {
        // 必须用writeAndFlush才会真正发出去
        if(socketChannel != null){
            socketChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(!channelFuture.isSuccess()){
                        log.info("发送失败");
                    }
                }
            });
        }
    }


    public synchronized void disconnect() {
        if (socketChannel != null && socketChannel.isActive()) {
            try {
                socketChannel.close().sync();
                socketChannel = null;
            } catch (InterruptedException e) {
                log.error("断连受到干扰：{}", ipAndPort, e);
            }
        }
    }

    public synchronized boolean isAlive(){
        return socketChannel != null && socketChannel.isActive();
    }

    public static NettyClient getClient(String address){
        NettyClient nettyClient = nettyClientMap.getOrDefault(address, null);
        if(nettyClient == null){
            nettyClient = new NettyClient(address);
            nettyClientMap.put(address, nettyClient);
        }

        return nettyClient;
    }

}
