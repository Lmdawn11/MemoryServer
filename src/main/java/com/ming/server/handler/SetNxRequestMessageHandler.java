package com.ming.server.handler;

import com.ming.message.set.SetRequestMessage;
import com.ming.message.set.SetResponseMessage;
import com.ming.message.setnx.SetNxRequestMessage;
import com.ming.message.setnx.SetNxResponseMessage;
import com.ming.server.config.SetConfig;
import com.ming.server.config.SetNxConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class SetNxRequestMessageHandler extends SimpleChannelInboundHandler<SetNxRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SetNxRequestMessage msg) throws Exception {
        String key = msg.getKey();
        String value = msg.getValue();
        int ttl = msg.getTtl();
        SetNxConfig setNxConfig = SetNxConfig.getInstance();
        String clientid = setNxConfig.setnx(key, value, ttl);
        if (clientid != null) {
            log.info("存储成功,key:{},value:{},ttl:{}", key, value, ttl);
            SetNxResponseMessage ok = new SetNxResponseMessage(true, clientid);
        }
        SetNxResponseMessage ok = new SetNxResponseMessage(false, clientid);
        log.info(ok.toString());
        ctx.writeAndFlush(ok);
    }
}
