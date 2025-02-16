package com.ming.server.handler;

import com.ming.message.del.DelRequestMessage;
import com.ming.message.del.DelResponseMessage;
import com.ming.server.config.SetConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class DelRequestMessageHandler extends SimpleChannelInboundHandler<DelRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelRequestMessage msg) throws Exception {
        SetConfig setShards = SetConfig.getSetConfig();
        String key = msg.getKey();
        Boolean delete = setShards.delete(key);
        DelResponseMessage resmsg;
        if (delete){
            resmsg = new DelResponseMessage(true, "ok");
        }else {
            resmsg = new DelResponseMessage(true, "no key");
        }
        ctx.writeAndFlush(resmsg);
        log.info(resmsg.toString());
    }
}
