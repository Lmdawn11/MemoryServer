package com.ming.server.handler;

import com.ming.message.set.SetRequestMessage;
import com.ming.message.set.SetResponseMessage;
import com.ming.message.setnx.SetNxRequestMessage;
import com.ming.message.setnx.SetNxResponseMessage;
import com.ming.server.config.SetConfig;
import com.ming.server.config.SetNxConfig;
import com.ming.server.ioc.SimpleIOC;
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
        SetNxConfig setNxConfig = SimpleIOC.getBean(SetNxConfig.class);
        String clientid = setNxConfig.setnx(key, value, ttl);
        SetNxResponseMessage ok;
        if (clientid != null) {
            log.info("存储成功,key:{},value:{},ttl:{}", key, value, ttl);
            ok = new SetNxResponseMessage(true, clientid);
        }else {
            ok = new SetNxResponseMessage(false, clientid);
        }
        log.info(ok.toString());
        ctx.writeAndFlush(ok);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}
