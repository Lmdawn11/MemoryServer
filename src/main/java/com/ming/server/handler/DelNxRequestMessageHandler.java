package com.ming.server.handler;

import com.ming.message.del.DelRequestMessage;
import com.ming.message.del.DelResponseMessage;
import com.ming.message.delnx.DelNxRequestMessage;
import com.ming.message.delnx.DelNxResponseMessage;
import com.ming.server.config.SetConfig;
import com.ming.server.config.SetNxConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class DelNxRequestMessageHandler extends SimpleChannelInboundHandler<DelNxRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DelNxRequestMessage msg) throws Exception {
        SetNxConfig setnxShards = SetNxConfig.getInstance();
        String key = msg.getKey();
        String clientId = msg.getClientId();
        Boolean delete = setnxShards.deleteNx(key,clientId);
        DelNxResponseMessage resmsg;
        if (delete){
            resmsg = new DelNxResponseMessage(true, "ok");
        }else {
            resmsg = new DelNxResponseMessage(false, "no key");
        }
        ctx.writeAndFlush(resmsg);
        log.info(resmsg.toString());
    }
}
