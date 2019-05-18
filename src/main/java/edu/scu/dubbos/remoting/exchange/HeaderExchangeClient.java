package edu.scu.dubbos.remoting.exchange;

import com.alibaba.fastjson.JSON;
import edu.scu.dubbos.remoting.transport.NettyClient;
import edu.scu.dubbos.rpc.Invocation;
import io.netty.channel.ChannelFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 20:47
 */
public class HeaderExchangeClient {
    private static final Logger logger= LogManager.getLogger(HeaderExchangeClient.class);
    private final NettyClient client;
    private static List<NettyClient> clients=new ArrayList<>();
    public HeaderExchangeClient(NettyClient client) {
        this.client = client;
        clients.add(client);
    }
    public DefaultFuture request(Invocation invocation) {
        Request req=new Request();
        req.setMessageData(invocation);
        DefaultFuture future=new DefaultFuture(client.getChannel(),req);
        send(req);
        return future;
    }
    private void send(Request request){
        logger.info("Sending request: ["+JSON.toJSONString(request,true)+"]");
        ChannelFuture future=client.getChannel().writeAndFlush(request);
    }
    public static void destroyAll(){
        for (NettyClient client:clients) {
            client.destroy();
        }
    }
    public void destroy(){
        client.destroy();
    }

}
