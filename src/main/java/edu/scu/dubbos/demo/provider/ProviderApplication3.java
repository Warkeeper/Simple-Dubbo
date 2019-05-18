package edu.scu.dubbos.demo.provider;

import edu.scu.dubbos.config.ApplicationConfig;
import edu.scu.dubbos.config.RegistryConfig;
import edu.scu.dubbos.config.ServiceConfig;
import edu.scu.dubbos.demo.api.DemoService;

import java.io.IOException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 16:15
 */
public class ProviderApplication3 {
    public static void main(String[] args) throws IOException {
        ServiceConfig<DemoServiceImpl3> service = new ServiceConfig<>();
        service.setApplication(new ApplicationConfig("DemoApplication"));
        service.setInterface(DemoService.class);
        service.setPort(1237);
        service.setRef(new DemoServiceImpl3());
        service.setRegistry(new RegistryConfig("127.0.0.1:2181"));
//        service.setNeedClientAuth(false);
//        service.setCert("server1-crt.pem");
//        service.setKey("server1-key.pem");
//        service.setRootCA("server-root-ca.crt");
        service.export();
        System.in.read();
    }
}
