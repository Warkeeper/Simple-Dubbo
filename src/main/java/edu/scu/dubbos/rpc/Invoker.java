package edu.scu.dubbos.rpc;

import edu.scu.dubbos.common.Node;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 14:06
 */
public interface Invoker<T> extends Node {

    Class<T> getInterface();

    Result invoke(Invocation invocation) throws RpcException;
}
