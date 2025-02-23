package com.ming.client.handler;

import com.ming.message.get.GetResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetResponseHandler extends SimpleChannelInboundHandler<GetResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetResponseMessage msg){
        String reason = msg.getReason();
        System.out.println("");
        log.info("get response: {}", reason);
        log.info(msg.toString());
        ctx.fireChannelRead(msg);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}
