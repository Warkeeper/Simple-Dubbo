package edu.scu.dubbos.remoting;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @Author: 杨斌
 * @Date: 2019/4/21 22:34
 */
public class RemotingException extends Exception{


    private SocketAddress localAddress;

    private SocketAddress remoteAddress;

    public RemotingException(Channel channel, String msg) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(),
                msg);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message) {
        super(message);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public RemotingException(Channel channel, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(),
                cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, Throwable cause) {
        super(cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public RemotingException(Channel channel, String message, Throwable cause) {
        this(channel == null ? null : channel.localAddress(), channel == null ? null : channel.remoteAddress(),
                message, cause);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message,
            Throwable cause) {
        super(message, cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
