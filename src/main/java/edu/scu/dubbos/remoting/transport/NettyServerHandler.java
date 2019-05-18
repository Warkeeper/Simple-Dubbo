package edu.scu.dubbos.remoting.transport;

import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.remoting.exchange.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author: 杨斌
 * @Date: 2019/4/21 23:28
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(NettyServerHandler.class);

    private ChannelHandler handler;

    public NettyServerHandler(ChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        //TODO: 用线程池来处理业务数据
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    handler.received(ctx.channel(), msg);
                } catch (RemotingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //        handler.received(ctx.channel(),msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handler.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handler.disconnected(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        handler.caught(ctx.channel(), cause);
    }
}
