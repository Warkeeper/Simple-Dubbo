package edu.scu.dubbos.rpc.protocol;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.remoting.exchange.HeaderExchangeClient;
import edu.scu.dubbos.rpc.Invocation;
import edu.scu.dubbos.rpc.Result;
import edu.scu.dubbos.rpc.RpcException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 20:03
 */
public class DubboInvoker<T> extends AbstractInvoker<T> {

    private final HeaderExchangeClient client;

    protected DubboInvoker(Class<T> type, URL url, HeaderExchangeClient client) {
        super(type, url);
        this.client=client;
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        try {
            return (Result) client.request(invocation).get();
        }catch (RemotingException e){
            throw new RpcException(RpcException.NETWORK_EXCEPTION,"Failed to invoke remote method: "
                    + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
    public void destroy(){
        client.destroy();
    }
}
