package edu.scu.dubbos.common;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 12:34
 */
public class Constants {
    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String DEFAULT_PROTOCOL = "dubbo";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String LOCALHOST_VALUE = "127.0.0.1";

    public static final String APPLICATION_KEY = "application";

    public static final String REGISTRY_KEY = "registry";

    public static final String INTERFACE_KEY = "interface";

    public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    public static final long DEFAULT_CONNECT_TIMEOUT = 20000;
    public static final long DEFAULT_TIMEOUT = 2000;
    public static final int DEFAULT_RETRIES = 2;

    public static final String TLS_NEED_CLIENT_AUTH_KEY = "needclientauth";

    public static final String TLS_SERVER_ROOT_CA_KEY = "rootca";

    public static final String TLS_SERVER_CERT_KEY = "servercert";

    public static final String TLS_SERVER_SECRET_KEY = "secretkey";

    public static final String TLS_CLIENT_ROOT_CA_KEY = "rootca";

    public static final String TLS_CLIENT_CERT_KEY = "clientcert";

    public static final String TLS_CLIENT_SECRET_KEY = "secretkey";

}
