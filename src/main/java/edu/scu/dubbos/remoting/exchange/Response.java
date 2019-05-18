package edu.scu.dubbos.remoting.exchange;

import java.io.Serializable;

/**
 * @Author: 杨斌
 * @Date: 2019/4/22 13:00
 */
public class Response implements Serializable {
    /**
     * ok.
     */
    public static final byte OK = 20;

    /**
     * clien side timeout.
     */
    public static final byte CLIENT_TIMEOUT = 30;

    /**
     * server side timeout.
     */
    public static final byte SERVER_TIMEOUT = 31;

    /**
     * channel inactive, directly return the unfinished requests.
     */
    public static final byte CHANNEL_INACTIVE = 35;

    /**
     * request format error.
     */
    public static final byte BAD_REQUEST = 40;

    /**
     * response format error.
     */
    public static final byte BAD_RESPONSE = 50;

    /**
     * service not found.
     */
    public static final byte SERVICE_NOT_FOUND = 60;

    /**
     * service error.
     */
    public static final byte SERVICE_ERROR = 70;

    /**
     * internal server error.
     */
    public static final byte SERVER_ERROR = 80;

    /**
     * internal server error.
     */
    public static final byte CLIENT_ERROR = 90;

    /**
     * server side threadpool exhausted and quick return.
     */
    public static final byte SERVER_THREADPOOL_EXHAUSTED_ERROR = 100;

    private long id = 0;

    private byte status = OK;
    private String errorMsg;
    private Object result;

    public Response() {
    }

    public Response(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override public String toString() {
        return "Response{" +
                "id=" + id +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", result=" + result +
                '}';
    }
}
