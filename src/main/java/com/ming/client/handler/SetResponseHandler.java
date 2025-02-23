package com.ming.client.handler;

import com.ming.message.Message;
import com.ming.message.set.SetResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetResponseHandler extends SimpleChannelInboundHandler<SetResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SetResponseMessage msg) {
        SetResponseMessage response = (SetResponseMessage) msg;
        log.info(response.toString());
        ctx.fireChannelRead(msg);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}