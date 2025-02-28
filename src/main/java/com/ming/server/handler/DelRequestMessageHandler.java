package com.ming.server.handler;

import com.ming.message.del.DelRequestMessage;
import com.ming.message.del.DelResponseMessage;
import com.ming.server.config.SetConfig;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class DelRequestMessageHandler extends SimpleChannelInboundHandler<DelRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelRequestMessage msg) throws Exception {
        SetConfig setShards = SimpleIOC.getBean(SetConfig.class);
        String key = msg.getKey();
        Boolean delete = setShards.delete(key);
        DelResponseMessage resmsg;
        if (delete){
            resmsg = new DelResponseMessage(true, "ok");
        }else {
            resmsg = new DelResponseMessage(false, "no key");
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
