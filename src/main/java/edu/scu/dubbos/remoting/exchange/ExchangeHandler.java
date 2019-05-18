package edu.scu.dubbos.remoting.exchange;

import edu.scu.dubbos.remoting.RemotingException;
import io.netty.channel.Channel;

/**
 * @Author: 杨斌
 * @Date: 2019/4/21 22:31
 */
public interface ExchangeHandler extends ChannelHandler{

    Object reply(Channel channel,Object request) throws RemotingException;
}
