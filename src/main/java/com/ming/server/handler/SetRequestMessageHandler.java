package com.ming.server.handler;

import com.ming.message.set.SetRequestMessage;
import com.ming.message.set.SetResponseMessage;
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
        SetConfig setShards = SetConfig.getSetConfig();
        setShards.set(key,value,ttl);
        log.info("存储成功,key:{},value:{},ttl:{}", key, value, ttl);
        SetResponseMessage ok = new SetResponseMessage(true, "OK");
        log.info(ok.toString());
        ctx.writeAndFlush(ok);
    }
}
