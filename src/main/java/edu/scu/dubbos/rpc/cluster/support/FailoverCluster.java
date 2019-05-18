package edu.scu.dubbos.rpc.cluster.support;

import edu.scu.dubbos.registry.RegistryDirectory;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.rpc.RpcException;
import edu.scu.dubbos.rpc.cluster.Cluster;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 17:37
 */
public class FailoverCluster implements Cluster {

    @Override
    public <T> Invoker<T> join(RegistryDirectory<T> directory) throws RpcException {
        return new FailoverClusterInvoker<T>(directory);
    }
}
