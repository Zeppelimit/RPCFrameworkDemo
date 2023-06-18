# DRPC
 
RPC是远程过程调用，为微服务框架提供远程调用服务。RPC框架的主要功能通过网络从远程计算机程序上请求服务，并隐藏网络
通信细节。DRPC框架主要使用Springboot框架开发，使用Netty进行网络通信，Zookeeper和Nacos作为注册中心和配置中心。DRPC支持
HTTP2和DRPC网络通讯协议，支持流式RPC，支持JSON，Kryo，JDK等序列化协议，支持服务的同步和异步调用，同时DRPC还具集群容
错、负载均衡、限流和熔断等机制。
