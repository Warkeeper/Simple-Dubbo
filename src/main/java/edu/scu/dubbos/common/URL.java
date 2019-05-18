package edu.scu.dubbos.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 11:46
 */
public class URL implements Serializable {

    private static final long serialVersionUID = -7856358971235281948L;
    private final String protocol;
    private final String host;
    private final int port;
    private final Map<String, String> parameters;

    public URL() {
        this.protocol = "dubbo";
        this.host = null;
        this.port = 0;
        this.parameters = new HashMap<String, String>();
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.parameters = parameters;
    }

    public URL(String host, int port) {
        this.host = host;
        this.port = port;
        this.parameters = new HashMap<String, String>();
        this.protocol = "dubbo";
    }

    public String getAddress() {
        return host + ":" + port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public URL setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(protocol, host, port, parameters);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public URL addParameter(String key, String value) {
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, host, port, map);
    }

    public URL removeParameter(String key) {
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.remove(key);
        return new URL(protocol, host, port, map);
    }

    public String toZkString() {
        StringBuilder buf = new StringBuilder();
        buf.append(protocol);
        buf.append("%3A%2F%2F");
        buf.append(host);
        buf.append("%3A");
        buf.append(port);
        buf.append("%2Fdubbo");
        buf.append(toParameterString());
        return buf.toString();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(protocol);
        buf.append("://");
        buf.append(host);
        buf.append(":");
        buf.append(port);
        buf.append("/dubbo");
        buf.append(toParameterString());
        return buf.toString();
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, true);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat) {
        boolean first = true;
        for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
            if (entry.getKey() != null && entry.getKey().length() > 0) {
                if (first) {
                    if (concat) {
                        buf.append("?");
                    }
                    first = false;
                } else {
                    buf.append("&");
                }
                buf.append(entry.getKey());
                buf.append("=");
                buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
            }
        }
    }

    public static URL parseUrl(String urlString) {
        String decodedUrl = decode(urlString);
        return valueOf(decodedUrl);
    }

    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?"); // seperator between body and parameters
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0)
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0)
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.lastIndexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0)
            host = url;
        return new URL(protocol, host, port, parameters);
    }
}
