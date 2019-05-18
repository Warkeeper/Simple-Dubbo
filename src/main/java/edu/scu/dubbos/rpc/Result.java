package edu.scu.dubbos.rpc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 14:06
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 280377708528686078L;

    private Object result;

    private Throwable exception;

    private Map<String, String> attachments = new HashMap<String, String>();

    public Result() {
    }

    public Result(Object result) {
        this.result = result;
    }

    public Result(Throwable exception) {
        this.exception = exception;
    }

    public Object recreate() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    public Object getValue() {
        return result;
    }

    public void setValue(Object value) {
        this.result = value;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable e) {
        this.exception = e;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    /**
     * Append all items from the map into the attachment, if map is empty then nothing happens
     *
     * @param map contains all key-value pairs to append
     */
    public void setAttachments(Map<String, String> map) {
        this.attachments = map == null ? new HashMap<String, String>() : map;
    }

    public void addAttachments(Map<String, String> map) {
        if (map == null) {
            return;
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        this.attachments.putAll(map);
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public String getAttachment(String key, String defaultValue) {
        String result = attachments.get(key);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }

    public void setAttachment(String key, String value) {
        attachments.put(key, value);
    }

    @Override
    public String toString() {
        return "Result [result=" + result + ", exception=" + exception + "]";
    }
}
