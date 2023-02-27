package com.li.transport.server;

import com.li.serialize.RpcDecoder;
import com.li.serialize.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import com.li.serialize.*;

@Slf4j
public class NettyServer {

    private static volatile NettyServer nettyServer;

    private int port;

    private final EventLoopGroup boss;

    private final EventLoopGroup worker;

    private Channel serverChannel;

    private boolean stated;

    private final EventExecutorGroup taskGroup =new DefaultEventExecutorGroup(2);

    public NettyServer() {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();
    }

    public void start(int port){
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            serverBootstrap.group(boss, worker)
                    .option(ChannelOption.SO_BACKLOG, 1024)	//设置TCP缓冲区
                    .option(ChannelOption.SO_RCVBUF, 32*1024) // 设置接受数据的缓存大小
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_SNDBUF, 32*1024) // 设置发送数据的缓存大小
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RpcDecoder(1024*1024*5,16,4))
                                    .addLast(new RpcEncoder())
                                    .addLast(new IdleStateHandler(0,0,200))
                                    .addLast(new HeartBeatRespHandler())
                                    .addLast(taskGroup, new NettyServerHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                    log.info("链路关闭: {}",channelFuture.channel().remoteAddress());
                }
            });

            serverChannel = channelFuture.channel();

            log.info("服务器绑定端口：{}成功", port);
            stated = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("bind port error:" + port, e);
        }
    }

    public void close(){
        serverChannel.close();
    }

    public static NettyServer getNettyServer() {
        if (nettyServer == null) {
            synchronized (NettyServer.class) {
                if(nettyServer ==null){
                    nettyServer = new NettyServer();
                }
            }
        }
        return nettyServer;
    }

}
