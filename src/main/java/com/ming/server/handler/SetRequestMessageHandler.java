package com.ming.server.handler;

import com.ming.message.SetRequestMessage;
import com.ming.message.SetResponseMessage;
import com.ming.server.config.SetConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class SetRequestMessageHandler extends SimpleChannelInboundHandler<SetRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SetRequestMessage msg) throws Exception {
        String key = msg.getKey();
        String value = msg.getValue();
        int ttl = msg.getTtl();
        SetConfig setShards = SetConfig.getInstance();
        setShards.set(key,value,ttl);
        log.info("存储成功,key:{},value:{},ttl:{}", key, value, ttl);
        ctx.writeAndFlush(new SetResponseMessage(true,"OK"));
    }
}
