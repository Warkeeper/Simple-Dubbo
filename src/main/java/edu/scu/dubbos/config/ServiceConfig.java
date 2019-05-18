package edu.scu.dubbos.config;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invoker;
import edu.scu.dubbos.utils.NetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 10:34
 */
public class ServiceConfig<T> extends AbstractConfig {

    static {
        DubbosShutdownHook.getDubbosShutdownHook().register();
    }

    private static final Logger logger = LogManager.getLogger(ServiceConfig.class);

    private T ref;

    private int port = 0;

    private Class<?> interfaceClass;

    private String cert;
    private String key;
    private boolean needClientAuth;
    private String rootCA;

    private boolean needExportPublic=false;
    private String publicAddress="";

    public ServiceConfig() {
    }

    public synchronized void export() {
        checkAndUpdateSubConfigs();
        URL url = generateURL();
        Invoker<?> invoker = jdkProxyFactory.getInvoker(ref, (Class) interfaceClass, url);
        registryProtocol.export(invoker);
        logger.info("Service [" + interfaceClass.getName() + "] exported successfully");
    }

    private URL generateURL() {
        String host = getHost();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.APPLICATION_KEY, applicationConfig.getName());
        parameters.put(Constants.REGISTRY_KEY, registryConfig.getAddress());
        parameters.put(Constants.INTERFACE_KEY, interfaceClass.getName());
        parameters.put(Constants.TLS_SERVER_CERT_KEY, cert);
        parameters.put(Constants.TLS_SERVER_SECRET_KEY, key);
        parameters.put(Constants.TLS_SERVER_ROOT_CA_KEY, rootCA);
        parameters.put(Constants.TLS_NEED_CLIENT_AUTH_KEY, Boolean.toString(needClientAuth));
        URL url = new URL(Constants.DEFAULT_PROTOCOL, host, port, parameters);
        return url;
    }

    private String getHost() {
        if(needExportPublic){
            return publicAddress;
        }
        String hostToBind = Constants.LOCALHOST_VALUE;
        try {
            hostToBind = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn(e.getMessage(), e);
        }
        if (NetUtils.isInvalidLocalHost(hostToBind)) {
            try (Socket socket = new Socket()) {
                SocketAddress address = new InetSocketAddress(registryConfig.getHost(), registryConfig.getPort());
                socket.connect(address, 1000);
                hostToBind = socket.getLocalAddress().getHostAddress();
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        if (NetUtils.isInvalidLocalHost(hostToBind)) {
            hostToBind = NetUtils.getLocalHost();
        }
        return hostToBind;
    }

    public void checkAndUpdateSubConfigs() {
        super.checkAndUpdateSubConfig();
        if (interfaceClass == null) {
            throw new IllegalStateException("Interface should be configured");
        }
        if (ref == null) {
            throw new IllegalStateException("Ref should be configured");
        }
        if (port == 0) {
            port = 28363;
        }
        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("Ref " + ref.getClass().getSimpleName()
                    + " is not implement of interface " + interfaceClass.getSimpleName());
        }
    }

    public void setInterface(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public void setRootCA(String rootCA) {
        this.rootCA = rootCA;
    }

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }

    public void setNeedExportPublic(boolean needExportPublic) {
        this.needExportPublic = needExportPublic;
    }
}
