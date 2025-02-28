package com.ming.server.handler;

import com.ming.message.delaynx.DelayNxRequestMessage;
import com.ming.message.delaynx.DelayNxResponseMessage;
import com.ming.server.config.SetNxConfig;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelayNxRequestMessageHandler extends SimpleChannelInboundHandler<DelayNxRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelayNxRequestMessage msg) throws Exception {
        SetNxConfig setNxConfig = SimpleIOC.getBean(SetNxConfig.class);
        String key = msg.getKey();
        int ttl = msg.getTtl();
        String clientId = msg.getClientId();
        boolean issuccess = setNxConfig.delayNX(key, ttl, clientId);
        if (issuccess) {
            ctx.writeAndFlush(new DelayNxResponseMessage(issuccess,"ok"));
        }else {
            ctx.writeAndFlush(new DelayNxResponseMessage(issuccess,"fail"));
        }
        log.info("对clientId:{}，中的key:{},续费ttl:{} ", clientId, key, ttl);
    }
}
