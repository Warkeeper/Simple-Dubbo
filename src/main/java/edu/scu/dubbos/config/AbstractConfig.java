package edu.scu.dubbos.config;

import edu.scu.dubbos.registry.RegistryProtocol;
import edu.scu.dubbos.rpc.proxy.JdkProxyFactory;

/**
 * @Author: 杨斌
 * @Date: 2019/4/17 13:43
 */
public abstract class AbstractConfig {

    protected ApplicationConfig applicationConfig;

    protected RegistryConfig registryConfig;

    RegistryProtocol registryProtocol=RegistryProtocol.getInstance();

    JdkProxyFactory jdkProxyFactory =new JdkProxyFactory();

    public void setApplication(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void setRegistry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }


    protected void checkAndUpdateSubConfig(){
        if (registryConfig == null) {
            throw new IllegalStateException("Registry should be configured");
        }
        registryConfig.checkAndUpdate();
        if (applicationConfig == null) {
            applicationConfig = new ApplicationConfig();
        }
        applicationConfig.checkAndUpdate();

    }

}
