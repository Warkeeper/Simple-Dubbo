package edu.scu.dubbos.rpc.protocol;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.common.utils.ConcurrentHashSet;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.remoting.exchange.ExchangeHandler;
import edu.scu.dubbos.remoting.exchange.HeaderExchangeClient;
import edu.scu.dubbos.remoting.exchange.HeaderExchangeHandler;
import edu.scu.dubbos.remoting.transport.NettyClient;
import edu.scu.dubbos.remoting.transport.NettyServer;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.RpcException;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 16:13
 */
public class DubboProtocol {
    private static final Logger logger = LogManager.getLogger(DubboProtocol.class);
    private static volatile DubboProtocol instance=null;
    //for providers
    private Map<String,Invoker> invokerMap=new ConcurrentHashMap<>();
    private Map<String,NettyServer> serverMap=new ConcurrentHashMap<>();
    //for consumers
    private Set<DubboInvoker> invokers=new ConcurrentHashSet<>();
    private ExchangeHandler requestHandler = new ExchangeHandler() {
        @Override
        public Object reply(Channel channel, Object request) throws RemotingException {
            if (request instanceof Invocation) {
                Invocation invocation = (Invocation) request;
                Invoker invoker = invokerMap.get(invocation.getAttachments().get(Constants.INTERFACE_KEY));
                return invoker.invoke(invocation);
            }
            throw new RemotingException(channel, "Unsupported request: "
                    + (request == null ? null : (request.getClass().getName() + ": " + request))
                    + ", channel: consumer: " + channel.remoteAddress() + " --> provider: " + channel.localAddress());
        }
        @Override
        public void connected(Channel channel) throws RemotingException {
            logger.info("Connected:["+channel.localAddress()+" -> "+channel.remoteAddress()+"]");
        }


        @Override
        public void disconnected(Channel channel) throws RemotingException {
            logger.info("Disconnected:["+channel.localAddress()+" -> "+channel.remoteAddress()+"]");
        }


        @Override
        public void sent(Channel channel, Object message) throws RemotingException {
            logger.info("Message:["+message+"] "+" ,sent:["+channel.localAddress()+" -> "+channel.remoteAddress()+"]");
        }


        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            if(message instanceof Invocation){
                reply(channel,message);
            }
        }

        @Override
        public void caught(Channel channel, Throwable exception) throws RemotingException {
            logger.error("Exception caught!",exception);
        }
    };

    public void export(Invoker invoker) throws RemotingException {
        String interfaceName=invoker.getInterface().getName();
        invokerMap.put(interfaceName,invoker);
        logger.info("DubboProtocol is exporting: " + invoker.getUrl());
        if (serverMap.get(interfaceName) == null){
            serverMap.put(interfaceName,new NettyServer(invoker.getUrl(),new HeaderExchangeHandler(requestHandler)));
        }
    }

    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException, RemotingException {
        logger.info("DubboProtocol is refering: " + url);
        DubboInvoker<T> invoker;
        invoker=new DubboInvoker<T>(serviceType,url
                    ,new HeaderExchangeClient(new NettyClient(new HeaderExchangeHandler(requestHandler),url)));
        invokers.add(invoker);
        return invoker;
    }

    private DubboProtocol() {
    }
    public void refreshInvokers(){
//        for (DubboInvoker invoker:invokers) {
//            invoker.destroy();
//        }
        invokers=new ConcurrentHashSet<>();
    }

    public static DubboProtocol getInstance(){
        if(instance==null){
            synchronized (DubboProtocol.class){
                if(instance==null){
                    instance=new DubboProtocol();
                }
            }
        }
        return instance;
    }
    public void destroy(){
        for (NettyServer server:serverMap.values()) {
            server.destroy();
        }
    }
}
