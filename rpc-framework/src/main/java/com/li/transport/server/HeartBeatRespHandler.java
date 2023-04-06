package com.li.transport.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import com.li.transport.client.struct.RpcHeader;
import com.li.transport.client.struct.RpcMessage;
import com.li.serialize.DataType;

@Slf4j
public class HeartBeatRespHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RpcMessage){
            RpcMessage rpcMessage = (RpcMessage) msg;
            RpcHeader rpcHeader = rpcMessage.getRpcHeader();
            byte isEvent = rpcHeader.getIsEvent();
            if(isEvent == DataType.HEART_BEAT.getValue()){
                log.info("发送pong，{}",rpcHeader.getId());
                ctx.writeAndFlush(buildHeatBeat(rpcHeader.getId()));
            }else{
                ctx.fireChannelRead(msg);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            log.info("关闭连接：{}", ctx.channel().remoteAddress());
            ctx.channel().close();
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    private RpcMessage buildHeatBeat(long id){
        RpcMessage rpcMessage = new RpcMessage();
        RpcHeader rpcHeader = new RpcHeader();
        rpcHeader.setMessageType((byte) DataType.HEART_BEAT.getValue());
        rpcHeader.setIsEvent((byte) 0);
        rpcHeader.setId(id);
        rpcMessage.setRpcHeader(rpcHeader);
        return rpcMessage;
    }


}
