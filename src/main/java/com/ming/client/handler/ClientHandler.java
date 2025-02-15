package com.ming.client.handler;

import com.ming.message.Message;
import com.ming.message.SetResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof SetResponseMessage) {
            SetResponseMessage response = (SetResponseMessage) msg;
            System.out.println("服务器返回: " + response.toString());
        }
    }
}