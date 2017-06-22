package com.genoprime.netty.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 *
 */
public class SimpleProxy {

    public static void main(String... args) throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        Channel channel;

        String host = "localhost";
        int portNumber = 8080;
        try {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(1));
                            pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
//                            pipeline.addLast("request-handler", new WebSocketServerProtocolHandler("/websocket"));
                            pipeline.addLast("handler", new DumbHandler());
                        }
                    });

            channel = serverBootstrap.bind(host, portNumber).sync().channel();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class DumbHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
            final String x = textWebSocketFrame.text();
            // uncomment to print request
            // /logger.info("Request received: {}", x);
            final String[] y = x.split(":");
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(y[0] + ":" + y[1].toUpperCase()));
        }
    }

}
