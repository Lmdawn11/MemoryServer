package com.ming.server.handler.listHandler;

import com.ming.message.list.pop.LPopRequestMessage;
import com.ming.message.list.pop.LPopResponseMessage;
import com.ming.server.config.ListConfig;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class LPopRequestMessageHandler extends SimpleChannelInboundHandler<LPopRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LPopRequestMessage msg) throws Exception {
        String key = msg.getKey();
        ListConfig listConfig = SimpleIOC.getBean(ListConfig.class);
        String value = listConfig.lpop(key);
        log.info("lpop key:{},value :{}", key, value);
        ctx.writeAndFlush(new LPopResponseMessage(true,value));
    }
}
