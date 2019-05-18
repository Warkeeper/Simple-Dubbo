package edu.scu.dubbos.config;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.Invoker;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 杨斌
 * @Date: 2019/4/17 13:14
 */
public class ReferenceConfig<T> extends AbstractConfig {

    static {
        DubbosShutdownHook.getDubbosShutdownHook().register();
    }

    private volatile T ref;

    private Invoker<T> invoker;

    private Class<T> interfaceClass;

    private String cert;
    private String key;
    private String rootCA;

    public ReferenceConfig() {
    }

    public synchronized T get() {
        checkAndUpdateSubConfigs();
        if (ref == null) {
            init();
        }
        return ref;
    }

    public void checkAndUpdateSubConfigs() {
        super.checkAndUpdateSubConfig();
        if (interfaceClass == null) {
            throw new IllegalStateException("Interface should be configured");
        }
    }

    private void init() {
        URL url = generateURL();
        invoker = registryProtocol.refer(interfaceClass, url);
        Class<?>[] classes = { interfaceClass };
        ref = (T) jdkProxyFactory.getProxy(invoker, classes);
    }

    private URL generateURL() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.APPLICATION_KEY, applicationConfig.getName());
        parameters.put(Constants.REGISTRY_KEY, registryConfig.getAddress());
        parameters.put(Constants.INTERFACE_KEY, interfaceClass.getName());
        parameters.put(Constants.TLS_CLIENT_CERT_KEY, cert);
        parameters.put(Constants.TLS_CLIENT_SECRET_KEY, key);
        parameters.put(Constants.TLS_CLIENT_ROOT_CA_KEY, rootCA);
        URL url = new URL(Constants.DEFAULT_PROTOCOL, "localhost", 0, parameters);
        return url;
    }

    public void setInterface(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setApplication(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void setRegistry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRootCA(String rootCA) {
        this.rootCA = rootCA;
    }
}
