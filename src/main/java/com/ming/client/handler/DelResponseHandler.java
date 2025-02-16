package com.ming.client.handler;

import com.ming.message.del.DelResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelResponseHandler extends SimpleChannelInboundHandler<DelResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelResponseMessage msg){
        log.info(msg.toString());
        ctx.fireChannelRead(msg);
    }
}
