package com.fjsh.rpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fjsh.rpc.annotation.RpcService;
import com.fjsh.rpc.common.RpcDecoder;
import com.fjsh.rpc.common.RpcEncoder;
import com.fjsh.rpc.common.RpcRequest;
import com.fjsh.rpc.common.RpcResponse;
import com.fjsh.rpc.utils.IpUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class RpcServer implements ApplicationContextAware, InitializingBean{

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;//当前服务地址信息
    private String serverport;//服务端口号
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<String, Object>(); // 存放接口名与服务对象之间的映射关系

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverport, ServiceRegistry serviceRegistry) {
    	this.serverAddress=IpUtil.getip();
        this.serverport = serverport;
        this.serviceRegistry = serviceRegistry;
    }
    public RpcServer(String serverAddress,String serverport, ServiceRegistry serviceRegistry) {
    	this.serverAddress=serverAddress;
        this.serverport = serverport;
        this.serviceRegistry = serviceRegistry;
    }
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class); // 获取所有带有 RpcService 注解的 Spring Bean
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
              	  private static final int READ_IDEL_TIME_OUT = 10; // 读超时
                  private static final int WRITE_IDEL_TIME_OUT = 0;// 写超时
                  private static final int ALL_IDEL_TIME_OUT = 0; // 所有超时
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        ((Channel) channel).pipeline()
                            .addLast(new RpcDecoder(RpcRequest.class)) // 将 RPC 请求进行解码（为了处理请求）
                            .addLast(new RpcEncoder(RpcResponse.class)) // 将 RPC 响应进行编码（为了返回响应）
                            .addLast(new IdleStateHandler(READ_IDEL_TIME_OUT,
                                    WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT, TimeUnit.SECONDS))
                            .addLast(new RpcServerHandler(handlerMap)); // 处理 RPC 请求
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);


            String host = serverAddress;
            int port = Integer.parseInt(serverport);
          //配置完成，绑定server，并通过sync同步方法阻塞直到绑定成功
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("server started on port {}", port);

            if (serviceRegistry != null) {
                serviceRegistry.register(host+":"+port); // 注册服务地址
            }
         // 等待服务器 socket 关闭 。在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。          
            future.channel().closeFuture().sync();//应用程序会一直等待，直到channel关闭  
          
          
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
