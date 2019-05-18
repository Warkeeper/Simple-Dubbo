package edu.scu.dubbos.rpc.cluster.support;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.registry.RegistryDirectory;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.Result;
import edu.scu.dubbos.rpc.RpcException;
import edu.scu.dubbos.rpc.cluster.loadbalance.RandomLoadBalance;
import edu.scu.dubbos.utils.NetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 17:40
 */
public class FailoverClusterInvoker<T> implements Invoker<T> {

    private static final Logger logger = LogManager.getLogger(FailoverClusterInvoker.class);

    private final RegistryDirectory<T> directory;

    public FailoverClusterInvoker(RegistryDirectory<T> directory) {
        this.directory = directory;
    }

    @Override
    public Class<T> getInterface() {
        return directory.getInterface();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        List<Invoker<T>> invokers = directory.list();
        checkInvokers(invokers, invocation);
        RpcException lastException = null;
        Invoker<T> unavailableInvoker=null;
        List<Invoker<T>> unInvokedInvokers = new ArrayList<>(invokers);
        int len = Constants.DEFAULT_RETRIES + 1;
        Set<String> providers = new HashSet<String>(len);
        for (int i = 0; i < invokers.size(); i++) {
            if(unavailableInvoker!=null){
                logger.warn("Provider "+unavailableInvoker.getUrl().getAddress()+" has called "
                +new Integer(Constants.DEFAULT_RETRIES+1)+" times, "
                        + "but still has no response, "
                        + "trying to call another one. "
                        + "Last error is "+lastException.getMessage(),lastException);
                lastException = null;
                unavailableInvoker=null;
            }
            Invoker<T> invoker = select(unInvokedInvokers);
            for (int j = 0; j < len; j++) {
                try {
                    Result result = invoker.invoke(invocation);
                    if (lastException != null) {
                        logger.warn("Although retry the method " + invocation.getMethodName()
                                + " in the service " + getInterface().getName()
                                + " was successful by the provider " + invoker.getUrl().getAddress()
                                + ", but there have been failed providers " + providers
                                + " (" + providers.size() + "/" + invokers.size()
                                + ") from the registry " + directory.getUrl().getAddress()
                                + " on the consumer " + NetUtils.getLocalHost()
                                + ". Last error is: "
                                + lastException.getMessage(), lastException);
                    }
                    return result;
                } catch (RpcException e) {
                    if (e.isBiz()) {
                        throw e;
                    }
                    lastException = e;
                } catch (Throwable e) {
                    lastException = new RpcException(e.getMessage(), e);
                } finally {
                    providers.add(invoker.getUrl().getAddress());
                }
            }
            unavailableInvoker=invoker;
        }
        throw new RpcException(lastException.getCode(), "Failed to invoke the method "
                + invocation.getMethodName() + " in the service " + getInterface().getName()
                + ". Tried " + len + " times for each of the providers " + providers
                + " (" + providers.size() + "/" + invokers.size()
                + ") from the registry " + directory.getUrl().getAddress()
                + " on the consumer " + NetUtils.getLocalHost() + ". Last error is: "
                + lastException.getMessage(), lastException.getCause() != null ? lastException.getCause() : lastException);
    }

    private Invoker<T> select(List<Invoker<T>> invokers) {
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        Invoker<T> invoker = RandomLoadBalance.select(invokers);

        return invoker;
    }

    private void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.isEmpty()) {
            throw new RpcException(
                    "No providers available now,which provide service [" + directory.getInterface().getName() + "]");
        }
    }

    @Override
    public URL getUrl() {
        return directory.getUrl();
    }
}
