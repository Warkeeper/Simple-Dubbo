package edu.scu.dubbos.remoting.exchange;

import edu.scu.dubbos.common.utils.StringUtils;
import edu.scu.dubbos.remoting.RemotingException;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 11:08
 */
public class HeaderExchangeHandler implements ChannelHandler{

    protected static final Logger logger= LogManager.getLogger(HeaderExchangeHandler.class);

    private final ExchangeHandler handler;

    public HeaderExchangeHandler(ExchangeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
    }

    private Response handleRequest(Channel channel, Request request) {
        Response response=new Response(request.getMessageId());
        Object msg=request.getMessageData();
        try {
            Object result=handler.reply(channel,msg);
            response.setStatus(Response.OK);
            response.setResult(result);
        } catch (Throwable e) {
            response.setStatus(Response.SERVER_ERROR);
            response.setErrorMsg(StringUtils.toString(e));
        }
        return response;
    }

    private void handleResponse(Channel channel, Response message) {
        if(message!=null){
            DefaultFuture.received(channel,message);
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        handler.disconnected(channel);
    }


    @Override
    public void sent(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        if(message instanceof Request){
            Response response=handleRequest(channel,(Request)message);
            channel.writeAndFlush(response);
        }else if(message instanceof Response){
            handleResponse(channel,(Response)message);
        }
    }


    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        logger.error("Exception caught from netty: ",exception);
        handler.caught(channel,exception);
    }
}
