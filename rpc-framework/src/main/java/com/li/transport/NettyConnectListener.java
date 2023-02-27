package com.li.transport;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.client.NettyClient;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyConnectListener implements ChannelFutureListener {

    private NettyClient nettyClient;

    public NettyConnectListener(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    log.info("服务端{}链接不上，开始重连操作...", nettyClient.getIpAndPort());
                    try {
                        nettyClient.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 3, TimeUnit.SECONDS);
        } else {
            log.info("连接成功, {}",nettyClient.getIpAndPort());
        }
    }
}
