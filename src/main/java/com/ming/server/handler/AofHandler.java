package com.ming.server.handler;

import com.ming.message.aofLogger.AOFLoggable;
import com.ming.message.del.DelRequestMessage;
import com.ming.message.get.GetRequestMessage;
import com.ming.message.set.SetRequestMessage;
import com.ming.server.config.AOFManager;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class AofHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        AOFManager aofManager = SimpleIOC.getBean(AOFManager.class);

        if (msg instanceof AOFLoggable){
            ((AOFLoggable) msg).logTo(aofManager);
            log.info("写入aof success");
        }
        // 解析 `Message` 类型，并记录 AOF
        if (msg instanceof SetRequestMessage) {
            SetRequestMessage setMsg = (SetRequestMessage) msg;
            aofManager.logCommand("set", setMsg.getKey(), setMsg.getValue(), String.valueOf(setMsg.getTtl()));
        } else if (msg instanceof GetRequestMessage) {
            GetRequestMessage getMsg = (GetRequestMessage) msg;
            aofManager.logCommand("get", getMsg.getKey());
        } else if (msg instanceof DelRequestMessage) {
            DelRequestMessage delMsg = (DelRequestMessage) msg;
            aofManager.logCommand("del", delMsg.getKey());
        }
        log.info("写入aof success");
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}
