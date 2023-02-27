package com.li.transport.client;

import com.li.transport.DefaultResponse;
import com.li.transport.ResponseCache;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import com.li.serialize.struct.RpcHeader;
import com.li.serialize.struct.RpcMessage;

@Slf4j
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcMessage){
            RpcMessage rpcMessage = (RpcMessage) msg;

            DefaultResponse response = (DefaultResponse) rpcMessage.getData();

            log.info("收到回复，id : {}",response.getId());
            ResponseCache.setResponsePromise(rpcMessage);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("服务端 {} 下线",ctx.channel().remoteAddress());
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("链接到服务端 {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
}
