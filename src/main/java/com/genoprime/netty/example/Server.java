package com.genoprime.netty.example;

import com.company.SomeHandler2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private Channel channel;

    public static void main(final String[] args) throws Exception {
        new Server().run();
    }

    public void run() throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());

            channel = serverBootstrap.bind("localhost", 8182).sync().channel();
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    private class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(final SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
            pipeline.addLast("aggregator", new HttpObjectAggregator(1));
            pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
            pipeline.addLast("request-handler", new WebSocketServerProtocolHandler("/websocket"));
            pipeline.addLast("handler", new SomeHandler());

            final ChannelPipeline pipelineParent = ch.parent().pipeline();
            pipelineParent.addLast("http-request-decoder-parent", new HttpRequestDecoder());
            pipelineParent.addLast("aggregator-parent", new HttpObjectAggregator(1));
            pipelineParent.addLast("http-response-encoder-parent", new HttpResponseEncoder());
            pipelineParent.addLast("handler-parent", new SomeHandler2());
        }
    }

    @ChannelHandler.Sharable
    public class SomeHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
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
