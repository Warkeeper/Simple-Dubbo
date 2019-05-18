package edu.scu.dubbos.rpc.cluster;

import edu.scu.dubbos.registry.RegistryDirectory;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.RpcException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 17:37
 */
public interface Cluster {

    <T> Invoker<T> join(RegistryDirectory<T> directory) throws RpcException;

}
