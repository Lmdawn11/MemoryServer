package com.ming.client;

import com.ming.client.handler.ClientHandler;
import com.ming.message.del.DelRequestMessage;
import com.ming.message.delaynx.DelayNxRequestMessage;
import com.ming.message.delnx.DelNxRequestMessage;
import com.ming.message.get.GetRequestMessage;
import com.ming.message.get.GetResponseMessage;
import com.ming.message.list.pop.LPopRequestMessage;
import com.ming.message.list.pop.RPopRequestMessage;
import com.ming.message.list.push.LPushRequestMessage;
import com.ming.message.list.push.RPushRequestMessage;
import com.ming.message.rewrite.RewriteRequestMessage;
import com.ming.message.set.SetRequestMessage;
import com.ming.message.set.SetResponseMessage;
import com.ming.message.setnx.SetNxRequestMessage;
import com.ming.protocol.MessageCodec;
import com.ming.protocol.MessageCodecSharable;
import com.ming.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MemoryClient {
    public void MemoryClient() {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
//        MessageCodec messageCodec = new MessageCodec();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        ProcotolFrameDecoder procotolFrameDecoder = new ProcotolFrameDecoder();
//        SetResponseHandler setResponseHandler = new SetResponseHandler();
//        GetResponseHandler getResponseHandler = new GetResponseHandler();
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
                    ch.pipeline().addLast(procotolFrameDecoder);
                    ch.pipeline().addLast(messageCodecSharable);
                    ch.pipeline().addLast("Response", clientHandler);
//                    ch.pipeline().addLast("setResponse", setResponseHandler);
//                    ch.pipeline().addLast("getResponse", getResponseHandler);
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
                                    System.out.println("rewrite");
                                    System.out.println("setnx key value ttl");
                                    System.out.println("delnx key clientId");
                                    System.out.println("delaynx key ttl, clientId");
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
                                        case "get":
                                            ctx.writeAndFlush(new GetRequestMessage(command[1]));
                                            break;
                                        case "del":
                                            ctx.writeAndFlush(new DelRequestMessage(command[1]));
                                            break;
                                        case "rewrite":
                                            ctx.writeAndFlush(new RewriteRequestMessage(command[0]));
                                            break;
                                        case "setnx":
                                            Integer i = Integer.valueOf(command[3]);
                                            ctx.writeAndFlush(new SetNxRequestMessage(command[1],command[2],i));
                                            break;
                                        case "delnx":
                                            ctx.writeAndFlush(new DelNxRequestMessage(command[1],"1"));
                                            break;
                                        case "delaynx":
                                            Integer ttl = Integer.valueOf(command[2]);
                                            ctx.writeAndFlush(new DelayNxRequestMessage(command[1],ttl,command[3]));
                                            break;
                                        case "lpush":
                                            String[] values = command[2].split(",");
                                            ctx.writeAndFlush(new LPushRequestMessage(command[1],values));
                                            break;
                                        case "rpush":
                                            values = command[2].split(",");
                                            ctx.writeAndFlush(new RPushRequestMessage(command[1],values));
                                            break;
                                        case "lpop":
                                            ctx.writeAndFlush(new LPopRequestMessage(command[1]));
                                            break;
                                        case "rpop":
                                            ctx.writeAndFlush(new RPopRequestMessage(command[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            break;
                                    }
                                }

                            },"system in ").start();
                        }
                    });

//                    // 单线程测试qps
//                    ch.pipeline().addLast("hi", new ChannelInboundHandlerAdapter() {
//                        private final AtomicInteger requestCount = new AtomicInteger(0);
//                        private final AtomicInteger responseCount = new AtomicInteger(0);
//                        private long startTime;
//                        private long endTime;
//
//                        @Override
//                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                            log.info("client connected");
//
//                            startTime = System.currentTimeMillis();
//                            endTime = startTime + 60_000; // **60 秒后停止**
//
//                            // **发送第一个请求**
//                            sendRequest(ctx);
//                        }
//
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            if (msg instanceof SetResponseMessage) {
//                                responseCount.incrementAndGet();
//
//                                // **如果时间未到，继续发送请求**
//                                if (System.currentTimeMillis() < endTime) {
//                                    sendRequest(ctx);
//                                } else {
//                                    // **时间到，计算 QPS**
//                                    int totalRequests = requestCount.get();
//                                    int totalResponses = responseCount.get();
//                                    log.info("[QPS Test] 60s 内发送请求: {}，收到响应: {}，客户端 QPS: {}，服务器 QPS: {}",
//                                            totalRequests, totalResponses, totalRequests / 60, totalResponses / 60);
//                                    ctx.channel().close();
//                                }
//                            }
//                        }
//
//                        private void sendRequest(ChannelHandlerContext ctx) {
//                            requestCount.incrementAndGet();
//                            ctx.writeAndFlush(new SetRequestMessage("test","ss"));
//                        }
//                    });



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
