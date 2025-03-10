package com.ming.server.handler;

import com.ming.message.rewrite.RewriteRequestMessage;
import com.ming.message.rewrite.RewriteResponseMessage;
import com.ming.server.config.AOFManager;
import com.ming.server.ioc.SimpleIOC;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RewriteRequestMessageHandler extends SimpleChannelInboundHandler<RewriteRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RewriteRequestMessage msg) {
        log.info("[AOF] 收到客户端请求，手动触发 AOF Rewrite...");
        AOFManager aofManager = SimpleIOC.getBean(AOFManager.class);

        try {
            aofManager.rewriteAOF();
            ctx.writeAndFlush(new RewriteResponseMessage(true, "AOF Rewrite 成功！"));
            log.info("[AOF] 手动 AOF Rewrite 完成！");
        } catch (Exception e) {
            log.error("[AOF] 手动 AOF Rewrite 失败！", e);
            ctx.writeAndFlush(new RewriteResponseMessage(false, "AOF Rewrite 失败：" + e.getMessage()));
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();  //发生异常时关闭连接
    }
}