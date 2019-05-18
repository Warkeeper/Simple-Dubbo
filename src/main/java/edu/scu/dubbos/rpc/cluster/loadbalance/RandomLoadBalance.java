package edu.scu.dubbos.rpc.cluster.loadbalance;

import edu.scu.dubbos.rpc.Invoker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: 杨斌
 * @Date: 2019/4/23 17:02
 */
public class RandomLoadBalance {
    public static <T> Invoker<T> select(List<Invoker<T>> invokers){
        int randomIndex= ThreadLocalRandom.current().nextInt(invokers.size());
        return invokers.remove(randomIndex);
    }
}
