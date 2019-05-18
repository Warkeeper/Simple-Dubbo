package edu.scu.dubbos.remoting.transport;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.remoting.exchange.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 21:13
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final URL url;

    private final ChannelHandler handler;

    private static final Logger logger= LogManager.getLogger(NettyClientHandler.class);

    public NettyClientHandler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handler.received(ctx.channel(),msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handler.caught(ctx.channel(),cause);
    }
}
