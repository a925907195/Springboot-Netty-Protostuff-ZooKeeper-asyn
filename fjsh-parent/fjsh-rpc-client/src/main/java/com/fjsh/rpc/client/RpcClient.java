package com.fjsh.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fjsh.rpc.common.RpcDecoder;
import com.fjsh.rpc.common.RpcEncoder;
import com.fjsh.rpc.common.RpcRequest;
import com.fjsh.rpc.common.RpcResponse;
import com.fjsh.rpc.connection.utils.PingMsg;

public class RpcClient  {

	private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
	//链接句柄，复用链接
	private static final ConcurrentHashMap<String, ChannelFuture> channelFutureConcurrentMap=new ConcurrentHashMap<String, ChannelFuture>();
//	FixedChannelPool fixedChannelPool=new FixedChannelPool(bootstrap, handler, maxConnections);
	   private static final int TRY_TIMES = 3;
	   protected final HashedWheelTimer timer = new HashedWheelTimer();
    private String host;
    private int port;
    private static int reqtimeout;//请求超时时间 
	private RpcResponse response;
	 private int currentTime = 0;
	 private Bootstrap bootstrap=new Bootstrap();
    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public RpcClient() {		
	}
	public void init(){  
      //  System.out.println("调用初始化方法....");  
    }
	ChannelFuture future=null;
public void connect(final int port,final String host) throws Exception {
	
	Channel channel=null;
	String keyString=String.valueOf(host+port);		
		//如果没有新建链接    	
	final RpcClientHandler rpcClientHandler=new RpcClientHandler(bootstrap, timer, host, port, true);

		 EventLoopGroup group = new NioEventLoopGroup(); 
	            bootstrap.group(group).channel(NioSocketChannel.class)
	                .handler(new ChannelInitializer<SocketChannel>() {
	                    @Override
	                    public void initChannel(SocketChannel channel) throws Exception {
	                        channel.pipeline()
	                          .addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
	                            .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
	                            .addLast( new IdleStateHandler(0, 2, 0, TimeUnit.SECONDS))
	                            .addLast(new RpcClientHandler(bootstrap,timer,host,port, true)); // 使用 RpcClient 发送 RPC 请求
	                    }
	                })
	                .option(ChannelOption.SO_KEEPALIVE, true);
	            try {
	             future = bootstrap.connect(host, port).sync();
	             RpcRequest request=new RpcRequest();
                 request.setBaseMsg(new PingMsg());
                 future.channel().writeAndFlush(request);
                 
	             NettyChannelLRUMap.add(keyString, (SocketChannel)future.channel());
	            } finally {
    }
}
    public RpcResponse send(RpcRequest request) throws Exception {
    	long start=System.currentTimeMillis();
    	String keyString=String.valueOf(host+port);	
    	while(true)
    	{
    		if(NettyChannelLRUMap.get(keyString)!=null)
    		{
    			Channel channel= NettyChannelLRUMap.get(keyString);
    			channel.writeAndFlush(request);    			
    			break;
    		}
    		else {
				connect(port, host);
			}
    	}
    	 for(int i=0;i<1000;i++)
    	 {    		 		
    		 Channel channel= NettyChannelLRUMap.get(keyString);
 			channel.writeAndFlush(request); 
 			
    	 }
//         synchronized (obj) {
//             obj.wait(reqtimeout); // 未收到响应，使线程等待2000ms
//         }
      
  
//         if (response != null) {
//             future.channel().closeFuture().sync();
//         }
         response=new RpcResponse();
         response.setResult("fjdkf");
         return response;      
    }
    public int getReqtimeout() {
		return reqtimeout;
	}

	public void setReqtimeout(int reqtimeout) {
		this.reqtimeout = reqtimeout;
	}

	
}
