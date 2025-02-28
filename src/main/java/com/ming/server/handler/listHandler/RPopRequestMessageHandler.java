package com.ming.server.handler.listHandler;

import com.ming.message.list.pop.LPopResponseMessage;
import com.ming.message.list.pop.RPopRequestMessage;
import com.ming.message.list.pop.RPopResponseMessage;
import com.ming.server.config.ListConfig;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class RPopRequestMessageHandler extends SimpleChannelInboundHandler<RPopRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPopRequestMessage msg) throws Exception {
        String key = msg.getKey();
        ListConfig listConfig = SimpleIOC.getBean(ListConfig.class);
        String value = listConfig.rpop(key);
        log.info("lpop key:{},value :{}", key, value);
        ctx.writeAndFlush(new RPopResponseMessage(true,value));
    }
}
