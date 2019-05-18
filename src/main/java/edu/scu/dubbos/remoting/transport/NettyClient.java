package edu.scu.dubbos.remoting.transport;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.common.utils.StringUtils;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.remoting.exchange.ChannelHandler;
import edu.scu.dubbos.remoting.exchange.Request;
import edu.scu.dubbos.utils.NetUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static edu.scu.dubbos.utils.NetUtils.getLocalAddress;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 20:55
 */
public class NettyClient {
    private static final Logger logger = LogManager.getLogger(NettyClient.class);
    private final NioEventLoopGroup nioEventLoopGroup
            = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS
            , new DefaultThreadFactory("NettyClientWorker", true));


//    private static final OioEventLoopGroup oioEvenLoopGroup=new OioEventLoopGroup();

    private Bootstrap bootstrap;

    private volatile Channel channel; // volatile, please copy reference to use

    private final ChannelHandler handler;

    private volatile URL url;

    private boolean needTls = false;

    private boolean clientAuth = false;

    private InputStream clientCert;

    private InputStream secretKey;

    private InputStream rootCa;

    public NettyClient(final ChannelHandler handler, final URL url) throws RemotingException {
        this.handler = handler;
        this.url = url;
        initTls();
        final NettyClientHandler nettyClientHandler = new NettyClientHandler(url, handler);
        bootstrap = new Bootstrap();
//        bootstrap.group(oioEvenLoopGroup)
//                .channel(OioSocketChannel.class);
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline channelPipeline = ch.pipeline();
                //SSL Support
                if (needTls) {
                    logger.info("SSL config detected,will build tls communication");
                    SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                            .sslProvider(SslProvider.OPENSSL)
                            .protocols("TLSv1.2")
                            .trustManager(rootCa);
                    if (clientAuth) {
                        sslContextBuilder.keyManager(clientCert, secretKey);
                    }
                    SslContext sslContext = sslContextBuilder.build();
                    final SslHandler sslHandler = sslContext.newHandler(ch.alloc());
                    channelPipeline.addFirst(sslHandler);
                    Future<Channel> handshakeFuture = sslHandler.handshakeFuture();
                    handshakeFuture.addListener(new FutureListener<Channel>() {
                        @Override
                        public void operationComplete(Future<Channel> future) throws Exception {
                            if (!future.isSuccess()) {
                                throw new IllegalStateException(future.cause());
                            }

                            Channel channel = future.getNow();

                            SSLEngine engine = sslHandler.engine();
                            SSLSession session = engine.getSession();
                            logger.info("Channel: [" + channel
                                    + "] established,using TLS protocol [" + session.getProtocol()
                                    + "],with cipher [" + session.getCipherSuite() + "]");
                        }
                    });
                }
                channelPipeline.addLast(new ObjectEncoder())
                        .addLast(new ObjectDecoder(ClassResolvers
                                .softCachingResolver(this.getClass().getClassLoader())))
                        .addLast(nettyClientHandler);
            }
        });
        logger.info("Connecting: " + getConnectAddress());
        ChannelFuture future = bootstrap.connect(getConnectAddress());
        try {
//            Thread.sleep(50);
            boolean ret = future.awaitUninterruptibly(Constants.DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (ret && future.isSuccess()) {
                channel = future.channel();
                logger.info("Connection to " + channel.remoteAddress() + " success");
            } else if (future.cause() != null) {
                logger.error("client (url: " + url + ") failed to connect to server "
                        + future.channel().remoteAddress() + ", error message is: ["
                        + future.cause().getMessage()+"]",future.cause());
                throw new RemotingException(future.channel(),
                        "client (url: " + url + ") failed to connect to server "
                                + future.channel().remoteAddress() + ", error message is:"
                                + future.cause().getMessage(), future.cause());
            } else {
                logger.error("client(url: " + url + ") failed to connect to server "
                        + future.channel().remoteAddress() + " client-side timeout "
                        + Constants.DEFAULT_CONNECT_TIMEOUT + "ms from netty client "
                        + NetUtils.getLocalHost());
                throw new RemotingException(future.channel(), "client(url: " + url + ") failed to connect to server "
                        + future.channel().remoteAddress() + " client-side timeout "
                        + Constants.DEFAULT_CONNECT_TIMEOUT + "ms from netty client "
                        + NetUtils.getLocalHost());
            }
        } finally {
        }
    }

    private void initTls() throws RemotingException {
        URL url = this.url;
        String rootCaUrl = url.getParameter(Constants.TLS_SERVER_ROOT_CA_KEY);
        if (StringUtils.isEmpty(rootCaUrl)) {
            logger.info("Consumer " + url.getParameter(Constants.INTERFACE_KEY)
                    + " has no tls config while trying to open netty 4.");
            needTls = false;
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                rootCa = classLoader.getResourceAsStream(rootCaUrl);
            } catch (NullPointerException npe) {
                throw new RemotingException(null, null, "Failed to connect " + getClass().getSimpleName()
                        + " on " + getLocalAddress()
                        + ", cause: tls context file [root ca] cannot find , please check if the file exists: "
                        + rootCaUrl);
            }
            String clientCertUrl = url.getParameter(Constants.TLS_CLIENT_CERT_KEY);
            String secretKeyUrl = url.getParameter(Constants.TLS_CLIENT_SECRET_KEY);
            if (StringUtils.isEmpty(clientCertUrl) || StringUtils.isEmpty(secretKeyUrl)) {
                logger.info("Consumer " + url.getParameter(Constants.INTERFACE_KEY)
                        + " has no tls cert or secret key ,may be unavailable to communicate a provider which need client auth.");
            } else {
                try {
                    clientCert = classLoader.getResourceAsStream(clientCertUrl);
                    secretKey = classLoader.getResourceAsStream(secretKeyUrl);
                } catch (NullPointerException npe) {
                    throw new RemotingException(null, null,
                            "Failed to connect " + getClass().getSimpleName()
                                    + " on " + getLocalAddress()
                                    + ", cause: tls context file [cert or secret key] cannot find , please check if these files exist: "
                                    + clientCertUrl + ", " + secretKeyUrl);
                }
                clientAuth = true;
            }
            needTls = true;
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(url.getHost(), url.getPort());
    }

    public void destroy(){
        logger.info("Shutting down netty client which connecting to [ "+channel.remoteAddress()+" ]");
        nioEventLoopGroup.shutdownGracefully();
    }
}
