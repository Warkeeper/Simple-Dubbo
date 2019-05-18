package edu.scu.dubbos.rpc.proxy;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.Result;
import edu.scu.dubbos.rpc.RpcException;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 14:09
 */
public abstract class AbstractProxyInvoker<T> implements Invoker{

    private final T proxy;

    private final Class<T> type;

    private final URL url;

    public AbstractProxyInvoker(T proxy, Class<T> type, URL url) {
        if (proxy == null) {
            throw new IllegalArgumentException("proxy == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("interface == null");
        }
        if (!type.isInstance(proxy)) {
            throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface " + type);
        }
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        try {
            return new Result(doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments()));
        } catch (InvocationTargetException e) {
            return new Result(e.getTargetException());
        } catch (Throwable e) {
            throw new RpcException("Failed to invoke remote proxy method " + invocation.getMethodName() + " to " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }


    public T getProxy() {
        return proxy;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    protected abstract Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments)
            throws Throwable;

    @Override
    public String toString() {
        return getInterface() + " -> " + (getUrl() == null ? " " : getUrl().toString());
    }

}
