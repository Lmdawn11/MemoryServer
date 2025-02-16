package com.ming.client.handler;

import com.ming.message.Message;
import com.ming.message.get.GetResponseMessage;
import com.ming.message.set.SetResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        log.info(msg.toString());
        ctx.fireChannelRead(msg);
    }
}
