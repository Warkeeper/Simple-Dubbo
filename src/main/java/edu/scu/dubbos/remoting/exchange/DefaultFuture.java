package edu.scu.dubbos.remoting.exchange;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.remoting.RemotingException;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 21:07
 */
public class DefaultFuture {

    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<>();
    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private final long id;
    private final Channel channel;
    private final Request request;
    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private volatile Response response;

    public DefaultFuture(Channel channel, Request req) {
        this.channel = channel;
        this.request = req;
        this.id = req.getMessageId();
        FUTURES.put(id, this);
        CHANNELS.put(id, channel);
    }

    public static DefaultFuture getFuture(long id) {
        return FUTURES.get(id);
    }

    public static boolean hasFuture(Channel channel) {
        return CHANNELS.containsValue(channel);
    }

    public static void received(Channel channel,Response response){
        try {
            DefaultFuture future=FUTURES.remove(response.getId());
            if(future!=null){
                future.doReceived(response);
            }
        }finally {
            CHANNELS.remove(response.getId());
        }
    }

    private void doReceived(Response res) {
        lock.lock();
        try {
            response = res;
            if (done != null) {
                done.signal();
            }
        } finally {
            lock.unlock();
        }
    }
    public Object get() throws RemotingException {
        if(!isDone()){
            long start = System.currentTimeMillis();
            lock.lock();
            try {
                while (!isDone()){
                    done.await(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                    if(isDone()||System.currentTimeMillis()-start>Constants.DEFAULT_TIMEOUT){
                        break;
                    }
                }
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }finally {
                lock.unlock();
            }
            if(!isDone()){
                throw new RemotingException(channel,"Can't get response from "+channel.remoteAddress()
                        +" in "+Constants.DEFAULT_TIMEOUT+"ms");
            }
        }
        return returnFromResponse();
    }

    private Object returnFromResponse() throws RemotingException {
        Response res=response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        if (res.getStatus() == Response.OK) {
            return res.getResult();
        }
        if (res.getStatus() == Response.CLIENT_TIMEOUT || res.getStatus() == Response.SERVER_TIMEOUT) {
            throw new RemotingException(channel, res.getErrorMsg());
        }
        throw new RemotingException(channel, res.getErrorMsg());
    }

    public boolean isDone(){
        return response!=null;
    }
}
