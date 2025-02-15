package com.ming.client;

import com.ming.client.handler.ClientHandler;
import com.ming.message.SetRequestMessage;
import com.ming.protocol.MessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class MemoryClient {
    public void MemoryClient() {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodec messageCodec = new MessageCodec();
        ClientHandler clientHandler = new ClientHandler();

        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(messageCodec);
                    ch.pipeline().addLast("hi",new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.info("client connected");
                            new Thread(()->{
                                while(true) {
                                    Scanner scanner = new Scanner(System.in);
                                    System.out.println("set key value ttl");
                                    System.out.println("get key");
                                    System.out.println("delete key");
                                    String line = scanner.nextLine();
                                    String[] command = line.split(" ");
                                    switch (command[0]) {
                                        case "set":
                                            if (command.length == 3) //no ttl
                                            {
                                                ctx.writeAndFlush(new SetRequestMessage(command[1],command[2]));
                                            }else if (command.length == 4){
                                                Integer i = Integer.valueOf(command[3]);
                                                ctx.writeAndFlush(new SetRequestMessage(command[1],command[2],i));
                                            }
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            break;
                                    }
                                }

                            },"system in ").start();
                        }
                    });
                    ch.pipeline().addLast("output",clientHandler);
                }

            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();


            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new MemoryClient().MemoryClient();
    }
}
