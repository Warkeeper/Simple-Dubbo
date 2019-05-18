package edu.scu.dubbos.rpc.protocol;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.Result;
import edu.scu.dubbos.rpc.RpcException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 19:57
 */
public abstract class AbstractInvoker<T> implements Invoker<T> {
    private static final Logger logger = LogManager.getLogger(AbstractInvoker.class);
    private final Class<T> type;
    private final URL url;

    protected AbstractInvoker(Class<T> type, URL url) {
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        invocation.setInvoker(this);
        try{
            return doInvoke(invocation);
        }catch (InvocationTargetException e){
            Throwable te=e.getTargetException();
            if(te==null){
                return new Result(e);
            }else {
                if(te instanceof RpcException){
                    ((RpcException)te).setCode(RpcException.BIZ_EXCEPTION);
                }
                return new Result(te);
            }
        }catch (RpcException e){
            if(e.isBiz()){
                return new Result(e);
            }else {
                throw e;
            }
        }catch (Throwable e){
            return new Result(e);
        }
    }

    protected abstract Result doInvoke(Invocation invocation) throws Throwable;

    @Override
    public URL getUrl() {
        return url;
    }
}
