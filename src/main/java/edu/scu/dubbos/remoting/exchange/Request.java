package edu.scu.dubbos.remoting.exchange;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 12:55
 */
public class Request implements Serializable {

    private static final AtomicLong MESSAGE_ID = new AtomicLong(0);

    private final long messageId;
    private Object messageData;

    public Request() {
        messageId = generateId();
    }

    private static long generateId() {
        return MESSAGE_ID.getAndIncrement();
    }

    public Request(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public Object getMessageData() {
        return messageData;
    }

    public void setMessageData(Object messageData) {
        this.messageData = messageData;
    }

    @Override public String toString() {
        return "Request{" +
                "messageId=" + messageId +
                ", messageData=" + messageData +
                '}';
    }
}
