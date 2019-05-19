# Simple Dubbo
本项目实现了一个简化版的Dubbo框架，旨在学习Dubbo的源码设计，大概了解Dubbo的实现过程，因为去除了微内核的设计，所以代码阅读和调试比较简单。（另外还增加了TLS支持）  
大致特性如下：  
1. 去除了Dubbo的微内核机制，指定了注册中心实现为Zookeeper、传输层实现为Netty4、负载均衡实现为简易版的RandomLoadbalance、容错实现为Failover with Retry、协议实现为Dubbo（实际上并不是）
2. 服务暴露和引用流程与Dubbo比较相似，主要的领域模型Invoker是和Dubbo保持几乎一致
3. 代理层实现指定为Jdk实现，官方源码导读花了比较大的时间讲述JavaAssist的实现过程，但对于想了解Dubbo怎么做的代理来说帮助比较小，其实直接看官方源码的Jdk实现就能明白代理层做了些啥（实际建议使用JavaAssist实现）
4. Dubbo里用了很多装饰者模式，在这个项目中借鉴了这样的设计，包括Invoker的装饰、Client、Server的装饰  
  
具体流程可以看本项目源码，大致了解过程后再去读Dubbo源码会有更好的理解  
  
本项目没有实现的内容如下：  
1. 微内核机制
2. FilterChain机制（类似拦截器）
3. 其它协议、注册中心实现、传输层、负载均衡、容错等可选的实现
4. 服务路由、JVM直连、线程模型等等较为高级的特性
5. Spring集成
6. 防御性编程

## 快速启动
本框架使用过程可参考src/main/edu.scu.dubbos.demo包下的用法  
### API
定义服务接口（Interface）  
注意：
1. 接口内定义的函数返回值和参数必须实现Serializable接口  
2. 服务提供者和消费者所使用的服务接口全类名必须相同（即除了类名、类所在包名也一致；建议API单独打包，在服务提供方和消费方共享）
  
DemoService.java:  
``
package edu.scu.dubbos.demo.api;

public interface DemoService {
    String hello(String name);
}
``
### 服务提供者
#### 在服务提供方实现接口：  
  
DemoServiceImpl.java:  
``
public class DemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        return "Hello," + name+" , this is DemoService1";
    }
}
``
#### 启动服务提供者
1. 服务提供者的服务暴露过程主要通过框架的ServiceConfig对象进行，对需要配置的配置项调用Setter后，调用该对象的export()函数即可完成服务暴露  
2. 必须配置的内容为：Registry（注册中心地址，限定为Zookeeper）、接口类、接口实现类  
ProviderApplication.java:  
``
public class ProviderApplication {
    public static void main(String[] args) throws IOException {

        ServiceConfig<DemoServiceImpl> service = new ServiceConfig<>();
        service.setApplication(new ApplicationConfig("DemoApplication"));
        service.setPort(1235);
        service.setRegistry(new RegistryConfig("127.0.0.1:2181"));
        service.setInterface(DemoService.class);
        service.setRef(new DemoServiceImpl());
        service.setNeedClientAuth(false);
        service.setCert("server1-crt.pem");
        service.setKey("server1-key.pem");
        service.setRootCA("server-root-ca.crt");
        service.export();

        System.in.read();
    }
}
``
### 服务消费者
#### 配置与调用远程服务
1. 服务消费者的远程服务对象的获取来自ReferenceConfig，当配置完毕ReferenceConfig后，调用该对象的get()方法就能获取到框架代理后的接口对象。
2. 必须配置的内容为：接口类、Registry（注册中心地址）  
ConsumerApplication.java:  
``
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
``

###其它内容
####关于TLS
在服务提供者端，如果需要TLS，则必须配置自己的证书链和证书对应的密钥，如果还配置了needClientAuth，则会对消费者做个认证，实际上就是在TLS握手的时候消费者会也发过来一个证书，所以还需要一个CA（证书颁发机构）证书来验证消费者。  
在服务消费者端，如果需要TLS，则必须配置一个CA证书（证书颁发机构的证书），用来验证服务端发来的证书。如果提供者端要对消费者认证，则还需要配置消费者端的证书和对应密钥。  
**本项目配套写了个证书签发中心，用于TLS相关证书的生成、签发和格式转换，具体内容可关注本人Github下的Repo**



