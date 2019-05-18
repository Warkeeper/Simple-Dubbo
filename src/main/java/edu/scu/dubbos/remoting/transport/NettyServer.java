package edu.scu.dubbos.remoting.transport;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.common.utils.StringUtils;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.remoting.exchange.ChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static edu.scu.dubbos.utils.NetUtils.getLocalAddress;

/**
 * @Author: 杨斌
 * @Date: 2019/4/21 22:24
 */
public class NettyServer {
    private static final Logger logger = LogManager.getLogger(NettyServer.class);
    private URL url;
    private ChannelHandler handler;

    private ServerBootstrap bootstrap;

    private Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private String serverCertUrl;
    private InputStream serverCert;

    private String secretKeyUrl;
    private InputStream secretKey;

    private String rootCaUrl;
    private InputStream rootCa;

    private boolean needClientAuth;

    private boolean needTls = false;

    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        this.url = url;
        this.handler = handler;
        initTls();
        openServer();
    }

    private void openServer() {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
        workerGroup = new NioEventLoopGroup((Constants.DEFAULT_IO_THREADS),
                new DefaultThreadFactory("NettyServerWorker", true));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        ChannelPipeline channelPipeline = ch.pipeline();
                        //SSL Support
                        if (needTls) {
                            serverCert.close();
                            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                            serverCert = classLoader.getResourceAsStream(serverCertUrl);
                            secretKey.close();
                            secretKey = classLoader.getResourceAsStream(secretKeyUrl);
                            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(serverCert, secretKey)
                                    .sslProvider(SslProvider.OPENSSL)
                                    .protocols("TLSv1.2");
                            if (needClientAuth) {
                                rootCa.close();
                                rootCa = classLoader.getResourceAsStream(rootCaUrl);
                                sslContextBuilder.clientAuth(ClientAuth.REQUIRE).trustManager(rootCa);
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
                                .addLast(new ObjectDecoder(ClassResolvers.
                                        softCachingResolver(this.getClass().getClassLoader())))
                                .addLast(new NettyServerHandler(handler));

                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(url.getPort());
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    private void initTls() throws RemotingException {
        String serverCertUrl = url.getParameter(Constants.TLS_SERVER_CERT_KEY);
        String secretKeyUrl = url.getParameter(Constants.TLS_SERVER_SECRET_KEY);
        if (StringUtils.isEmpty(serverCertUrl) || StringUtils.isEmpty(secretKeyUrl)) {
            logger.info("Provider " + url.getParameter(Constants.INTERFACE_KEY)
                    + " has no tls config while trying to open netty 4.");
            needTls = false;
        } else {
            String needClientAuthUrl = url.getParameter(Constants.TLS_NEED_CLIENT_AUTH_KEY);
            if (StringUtils.isNotEmpty(needClientAuthUrl)) {
                needClientAuth = Boolean.valueOf(needClientAuthUrl);
                ;
            } else {
                needClientAuth = false;
            }
            String rootCaUrl = null;
            if (needClientAuth) {
                rootCaUrl = url.getParameter(Constants.TLS_SERVER_ROOT_CA_KEY);
                if (StringUtils.isEmpty(rootCaUrl)) {
                    throw new RemotingException(null, null, "Failed to bind " + getClass().getSimpleName()
                            + " on " + getLocalAddress() + ", cause: needClientAuth is true but root ca is not configured");
                }
            }
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                serverCert = classLoader.getResourceAsStream(serverCertUrl);
                this.serverCertUrl = serverCertUrl;
                secretKey = classLoader.getResourceAsStream(secretKeyUrl);
                this.secretKeyUrl = secretKeyUrl;
                if (needClientAuth) {
                    rootCa = classLoader.getResourceAsStream(rootCaUrl);
                    this.rootCaUrl = rootCaUrl;
                }
            } catch (NullPointerException npe) {
                throw new RemotingException(null, null, "Failed to bind " + getClass().getSimpleName()
                        + " on " + getLocalAddress()
                        + ", cause: tls context file cannot find , please check if these files exist: "
                        + serverCertUrl + ", " + secretKeyUrl + ", " + rootCaUrl);
            }
            needTls = true;
        }
    }

    public void destroy() {
        logger.info("Shutting down netty server which provides service [ " + url.getParameter(Constants.INTERFACE_KEY) + " ] ,"
                + "binding at [" + url.getPort() + "]");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
