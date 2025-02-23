package com.ming.server.handler;

import com.ming.message.get.GetRequestMessage;
import com.ming.message.get.GetResponseMessage;
import com.ming.server.config.SetConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class GetRequestMessageHandler extends SimpleChannelInboundHandler<GetRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetRequestMessage msg) throws Exception {
        SetConfig setShards = SetConfig.getSetConfig();
        String key = msg.getKey();
        String s = setShards.get(key);
        GetResponseMessage resmsg;
        if (s != null){
            resmsg = new GetResponseMessage(true, s);
        }else {
            resmsg = new GetResponseMessage(true, "no key");
        }
        ctx.writeAndFlush(resmsg);
        log.info(resmsg.toString());
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}
