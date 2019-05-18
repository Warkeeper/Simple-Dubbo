package edu.scu.dubbos.registry;

import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.remoting.RemotingException;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.RpcException;
import edu.scu.dubbos.rpc.cluster.Cluster;
import edu.scu.dubbos.rpc.cluster.support.FailoverCluster;
import edu.scu.dubbos.rpc.protocol.DubboProtocol;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 12:58
 */
public class RegistryProtocol {

    private static volatile RegistryProtocol instance=null;
    private ZookeeperRegistry registry=null;
    private DubboProtocol dubboProtocol=DubboProtocol.getInstance();
    private Cluster cluster=new FailoverCluster();

    public void export(final Invoker invoker) throws RpcException{

        registry=new ZookeeperRegistry(invoker.getUrl());
        registry.register(invoker.getUrl());
        try {
            dubboProtocol.export(invoker);
        } catch (RemotingException e) {
            throw new RpcException(e);
        }
    }

    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException{
        RegistryDirectory<T> directory=new RegistryDirectory<T>(type,url);
        directory.subscribe(url);
//        System.out.println("Subscribe success,got these results: "+directory.list());
        Invoker invoker=cluster.join(directory);
        return invoker;
    }
    private RegistryProtocol(){

    }
    public static RegistryProtocol getInstance(){
        if(instance==null){
            synchronized (RegistryProtocol.class){
                if(instance==null){
                    instance=new RegistryProtocol();
                }
            }
        }
        return instance;
    }
}
