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
