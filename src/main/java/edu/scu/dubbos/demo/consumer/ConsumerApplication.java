package edu.scu.dubbos.demo.consumer;

import edu.scu.dubbos.config.ApplicationConfig;
import edu.scu.dubbos.config.ReferenceConfig;
import edu.scu.dubbos.config.RegistryConfig;
import edu.scu.dubbos.demo.api.DemoService;

import java.io.IOException;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 16:15
 */
public class ConsumerApplication {
    public static void main(String[] args) throws IOException, InterruptedException {

        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig("DemoApplication"));
        reference.setInterface(DemoService.class);
        reference.setRegistry(new RegistryConfig("127.0.0.1:2181"));
        reference.setCert("client1-crt.pem");
        reference.setKey("client1-key.pem");
        reference.setRootCA("client-root-ca.crt");

        final DemoService demoService = reference.get();

        while (true) {
            String result = demoService.hello("Warkeeper");
            System.out.println(result);
            Thread.sleep(2000);
        }
    }
}
