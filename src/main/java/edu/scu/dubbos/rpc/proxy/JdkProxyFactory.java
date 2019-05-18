package edu.scu.dubbos.rpc.proxy;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invoker;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**z
 * @Author: 杨斌
 * @Date: 2019/4/16 14:04
 */
public class JdkProxyFactory implements ProxyFactory {
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                , interfaces, new InvokerInvocationHandler(invoker));
    }
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
        return new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName,
                    Class<?>[] parameterTypes,
                    Object[] arguments) throws Throwable {
                Method method = proxy.getClass().getMethod(methodName, parameterTypes);
                return method.invoke(proxy, arguments);
            }
        };
    }
}
