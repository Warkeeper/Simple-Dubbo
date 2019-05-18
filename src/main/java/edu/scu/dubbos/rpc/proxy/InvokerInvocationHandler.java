package edu.scu.dubbos.rpc.proxy;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 14:24
 */
public class InvokerInvocationHandler implements InvocationHandler {
    private final Invoker<?> invoker;
    public <T> InvokerInvocationHandler(Invoker<T> invoker) {
        this.invoker=invoker;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        Map<String,String> attachments=new HashMap<>(1);
        attachments.put(Constants.INTERFACE_KEY,invoker.getInterface().getName());
        return invoker.invoke(new Invocation(method, args,attachments)).recreate();
    }
}
