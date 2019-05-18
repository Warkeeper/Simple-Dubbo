package edu.scu.dubbos.rpc.proxy;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invoker;

/**
 * @Author: 杨斌
 * @Date: 2019/4/28 13:16
 */

public interface ProxyFactory {

    <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces);

    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url);
}
