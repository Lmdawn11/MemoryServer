package com.ming.server;

import com.ming.protocol.MessageCodec;
import com.ming.protocol.MessageCodecSharable;
import com.ming.protocol.ProcotolFrameDecoder;
import com.ming.server.handler.*;
import com.ming.server.handler.listHandler.LPopRequestMessageHandler;
import com.ming.server.handler.listHandler.LPushRequestMessageHandler;
import com.ming.server.handler.listHandler.RPopRequestMessageHandler;
import com.ming.server.handler.listHandler.RPushRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryServer {
    private final int port;

    public MemoryServer(int port) {
        this.port = port;
    }

    public void StartServer(){
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
//        MessageCodec messageCodec = new MessageCodec();
        ProcotolFrameDecoder procotolFrameDecoder = new ProcotolFrameDecoder();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        AofHandler aofHandler = new AofHandler();
        SetRequestMessageHandler setRequestMessageHandler = new SetRequestMessageHandler();
        GetRequestMessageHandler getRequestMessageHandler = new GetRequestMessageHandler();
        DelRequestMessageHandler delRequestMessageHandler = new DelRequestMessageHandler();
        RewriteRequestMessageHandler rewriteRequestMessageHandler = new RewriteRequestMessageHandler();
        SetNxRequestMessageHandler setNxRequestMessageHandler = new SetNxRequestMessageHandler();
        DelNxRequestMessageHandler delNxRequestMessageHandler = new DelNxRequestMessageHandler();
        LPushRequestMessageHandler lPushRequestMessageHandler = new LPushRequestMessageHandler();
        RPushRequestMessageHandler rPushRequestMessageHandler = new RPushRequestMessageHandler();
        RPopRequestMessageHandler rPopRequestMessageHandler = new RPopRequestMessageHandler();
        LPopRequestMessageHandler lPopRequestMessageHandler = new LPopRequestMessageHandler();

        NioEventLoopGroup bosses = new NioEventLoopGroup(1);
        NioEventLoopGroup workers = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bosses,workers);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(procotolFrameDecoder);
                    ch.pipeline().addLast(messageCodecSharable);
                    ch.pipeline().addLast(aofHandler);
                    ch.pipeline().addLast(setRequestMessageHandler);
                    ch.pipeline().addLast(getRequestMessageHandler);
                    ch.pipeline().addLast(delRequestMessageHandler);
                    ch.pipeline().addLast(rewriteRequestMessageHandler);
                    ch.pipeline().addLast(setNxRequestMessageHandler);
                    ch.pipeline().addLast(delNxRequestMessageHandler);
                    ch.pipeline().addLast(lPushRequestMessageHandler);
                    ch.pipeline().addLast(rPushRequestMessageHandler);
                    ch.pipeline().addLast(rPopRequestMessageHandler);
                    ch.pipeline().addLast(lPopRequestMessageHandler);
                }

            });
            Channel channel = serverBootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            bosses.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new MemoryServer(8080).StartServer();
    }
}
