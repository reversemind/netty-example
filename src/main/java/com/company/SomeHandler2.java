package com.company;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;

/**
 *
 */
@ChannelHandler.Sharable
public class SomeHandler2 extends SimpleChannelInboundHandler<HttpContent> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpContent content) throws Exception {
        System.out.println("http content:" + content.copy());
        channelHandlerContext.writeAndFlush(content);
    }
}
