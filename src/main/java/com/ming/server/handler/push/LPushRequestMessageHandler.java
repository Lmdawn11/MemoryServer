package com.ming.server.handler.push;

import com.ming.message.list.push.LPushRequestMessage;
import com.ming.message.list.push.LPushResponseMessage;
import com.ming.server.config.ListConfig;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class LPushRequestMessageHandler extends SimpleChannelInboundHandler<LPushRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LPushRequestMessage msg) throws Exception {
        ListConfig listConfig = SimpleIOC.getBean(ListConfig.class);
        String key = msg.getKey();
        String[] values = msg.getValues();
        listConfig.lpush(key, values);
        log.info("存储 lpush key:{},values:{}", key, values);
        ctx.writeAndFlush(new LPushResponseMessage(true,"ok"));
    }
}
