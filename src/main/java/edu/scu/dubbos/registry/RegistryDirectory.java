package edu.scu.dubbos.registry;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.protocol.DubboProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 杨斌
 * @Date: 2019/4/17 15:29
 */
public class RegistryDirectory<T> implements ChildListener {
    private static final Logger logger = LogManager.getLogger(RegistryDirectory.class);
    private Class<T> serviceType;
    private URL url;
    private DubboProtocol dubboProtocol = DubboProtocol.getInstance();
    private volatile Map<String, List<Invoker<T>>> invokersMap = new ConcurrentHashMap<>();
    private volatile List<Invoker<T>> invokerList = new ArrayList<>();
    private ZookeeperRegistry registry;

//    private final AtomicBoolean isRefreshing=new AtomicBoolean(false);
    private CountDownLatch lock=new CountDownLatch(0);
    //TODO: Delete me later
    private Map<String, List<String>> subscribeResult = new ConcurrentHashMap<>();

    public RegistryDirectory(Class<T> serviceType, URL url) {
        this.serviceType = serviceType;
        this.url = url;
        registry = new ZookeeperRegistry(url);
    }

    public void subscribe(URL url) {
        List<String> result = registry.subscribe(url, this);
        refreshInvokers(result);
        subscribeResult.put(url.getParameter(Constants.INTERFACE_KEY), result);
    }

    private void refreshInvokers(List<String> invokersUrl) {
        dubboProtocol.refreshInvokers();
        List<Invoker<T>> invokers = new ArrayList<>();
        if (invokersUrl == null || invokersUrl.isEmpty()) {
            invokerList = invokers;
            invokersMap.put(url.getParameter(Constants.INTERFACE_KEY), invokers);
            return;
        }
        for (String urlString : invokersUrl) {
            URL invokerUrl = URL.parseUrl(urlString);
            Invoker<T> invoker = null;
            try {
                invoker = dubboProtocol.refer(serviceType, mergeUrl(invokerUrl,url));
                Thread.sleep(1000);
            } catch (RemotingException e) {
                logger.warn("Invoker ["+invokerUrl+"] cannot create,dubbos will skip this one");
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            invokers.add(invoker);
        }
        invokerList = invokers;
        invokersMap.put(url.getParameter(Constants.INTERFACE_KEY), invokers);
    }

    @Override
    public void childChanged(String path, final List<String> children) {
        lock=new CountDownLatch(1);
        subscribeResult.put(url.getParameter(Constants.INTERFACE_KEY), children);
        logger.info("Provider status changed: " + subscribeResult+" ,refreshing...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshInvokers(children);
                lock.countDown();
            }
        },"RefreshInvoker-Thread").start();
    }

    public List<Invoker<T>> list() {
        try {
            lock.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting refreshing Invoker",e);
        }
        return invokerList;
    }

    public Class<T> getInterface() {
        return serviceType;
    }

    public URL getUrl() {
        return url;
    }

    private URL mergeUrl(URL invokerUrl,URL originUrl){
        URL newUrl=invokerUrl.addParameter(Constants.TLS_CLIENT_ROOT_CA_KEY
                ,originUrl.getParameter(Constants.TLS_CLIENT_ROOT_CA_KEY))
                .addParameter(Constants.TLS_CLIENT_SECRET_KEY
                        ,originUrl.getParameter(Constants.TLS_CLIENT_SECRET_KEY))
                .addParameter(Constants.TLS_CLIENT_CERT_KEY
                        ,originUrl.getParameter(Constants.TLS_CLIENT_CERT_KEY));
        return newUrl;
    }
}
