package com.ming.server;

import com.ming.message.rewrite.RewriteRequestMessage;
import com.ming.protocol.MessageCodec;
import com.ming.server.config.SetConfig;
import com.ming.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
        MessageCodec messageCodec = new MessageCodec();
        AofHandler aofHandler = new AofHandler();
        SetRequestMessageHandler setRequestMessageHandler = new SetRequestMessageHandler();
        GetRequestMessageHandler getRequestMessageHandler = new GetRequestMessageHandler();
        DelRequestMessageHandler delRequestMessageHandler = new DelRequestMessageHandler();
        RewriteRequestMessageHandler rewriteRequestMessageHandler = new RewriteRequestMessageHandler();
        NioEventLoopGroup bosses = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bosses,workers);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(messageCodec);
                    ch.pipeline().addLast(aofHandler);
                    ch.pipeline().addLast(setRequestMessageHandler);
                    ch.pipeline().addLast(getRequestMessageHandler);
                    ch.pipeline().addLast(delRequestMessageHandler);
                    ch.pipeline().addLast(rewriteRequestMessageHandler);
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
