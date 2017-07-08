package com.fjsh.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fjsh.rpc.common.RpcDecoder;
import com.fjsh.rpc.common.RpcEncoder;
import com.fjsh.rpc.common.RpcRequest;
import com.fjsh.rpc.common.RpcResponse;
import com.fjsh.rpc.connection.utils.BaseMsg;
import com.fjsh.rpc.connection.utils.MsgType;
import com.fjsh.rpc.connection.utils.PingMsg;
import io.netty.channel.ChannelHandler.Sharable;
@Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> implements TimerTask {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
	//链接句柄，复用链接
	private static final ConcurrentHashMap<String, ChannelFuture> channelFutureConcurrentMap=new ConcurrentHashMap<String, ChannelFuture>();
//	FixedChannelPool fixedChannelPool=new FixedChannelPool(bootstrap, handler, maxConnections);
	private static final int TRY_TIMES = 3;
	 private final Bootstrap bootstrap;
    private String host;
    private int port;
    private static int reqtimeout;//请求超时时间 
	private RpcResponse response;
	// private int currentTime = 0;
    private final Object obj = new Object();
    private int attempts;//重试次数
    private final Timer timer;//定时器
    public ChannelHandler[] handlers;
    private volatile boolean reconnect = true;
    
    public RpcClientHandler(Bootstrap bootstrap,Timer timer, String host, int port,
			  boolean reconnect) {
		super();
		this.bootstrap = bootstrap;
		this.host = host;
		this.port = port;		
		this.timer = timer;
		this.reconnect = reconnect;
		handlers=new ChannelHandler[] {
                this,
                new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
                new RpcEncoder(RpcRequest.class),
                new RpcDecoder(RpcResponse.class)};
	}
		 @Override
	    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	        if (evt instanceof IdleStateEvent) {
	            IdleStateEvent event = (IdleStateEvent) evt;
	            if (event.state() == IdleState.WRITER_IDLE) {
	               // if(currentTime <= TRY_TIMES){
	                    System.out.println("心跳触发时间："+new Date()+"heart beat currentTime:");
	                   // currentTime++;
	                    RpcRequest request=new RpcRequest();
	                    request.setBaseMsg(new PingMsg());
	                    ctx.channel().writeAndFlush(request);
	              //  }
	            }
	        }
	    }
    @Override
	protected void messageReceived(ChannelHandlerContext arg0, RpcResponse rpcResponse)
			throws Exception {
		// TODO Auto-generated method stub
    	BaseMsg baseMsg=rpcResponse.getBaseMsg();
        MsgType msgType=baseMsg.getType();
        switch (msgType){
            case LOGIN:{
//                //向服务器发起登录
//                LoginMsg loginMsg=new LoginMsg();
//                loginMsg.setPassword("yao");
//                loginMsg.setUserName("robin");
//                channelHandlerContext.writeAndFlush(response);
            }break;
            case PING:{
                logger.debug("receive ping from server----------");
            }break;
            case ASK:{
              /*  ReplyClientBody replyClientBody=new ReplyClientBody("client info **** !!!");
                ReplyMsg replyMsg=new ReplyMsg();
                replyMsg.setBody(replyClientBody);
                channelHandlerContext.writeAndFlush(replyMsg);*/
            }break;
            case REPLY:{
            	 this.response = rpcResponse; 
            	 System.out.println(rpcResponse.getResult());
            }
            default:break;
        }
   	 synchronized (obj) {
            obj.notifyAll(); // 收到响应，唤醒线程
        }
	}
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelLRUMap.remove((SocketChannel)ctx.channel());
        System.out.println("链接关闭");
        if(reconnect){
            System.out.println("链接关闭，将进行重连");
            if (attempts < 12) {
                attempts++;
                //重连的间隔时间会越来越长
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
            }
        }
        ctx.fireChannelInactive();
    }    
    /**
     * channel链路每次active的时候，将其连接的次数重新☞ 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        
        System.out.println("当前链路已经激活了，重连尝试次数重新置为0");
        
        attempts = 0;
        //判断连接结果，如果没有连接成功，则监听连接网络操作位SelectionKey.OP_CONNECT。如果连接成功，则调用pipeline().fireChannelActive()将监听位修改为READ。
        ctx.fireChannelActive();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception", cause);
        NettyChannelLRUMap.remove((SocketChannel)ctx.channel());
        ctx.close();
    }    
//    public RpcResponse send(RpcRequest request) throws Exception {
//    	long start=System.currentTimeMillis();
//    	ChannelFuture future=null;
//    	Channel channel=null;
//    	String keyString=String.valueOf(host+port);
//    	 //自己定义
//        AskMsg askMsg=new AskMsg();    
//        askMsg.setClientId(keyString);
//        request.setBaseMsg(askMsg);
//    	if(NettyChannelLRUMap.get(keyString) != null)
//    	{
//    		channel=(Channel) NettyChannelLRUMap.get(keyString);
//    	}
//    	else {
//			//如果没有新建链接    	
//    		 EventLoopGroup group = new NioEventLoopGroup();   	      
//    		
//    	            bootstrap.group(group).channel(NioSocketChannel.class)
//    	                .handler(new ChannelInitializer<SocketChannel>() {
//    	                    @Override
//    	                    public void initChannel(SocketChannel channel) throws Exception {
//    	                        channel.pipeline().addLast(handlers);
////    	                            .addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
////    	                            .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
////    	                            .addLast( new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS))
////    	                            .addLast(new RpcClientHandler(bootstrap,timer,host,port, true)); // 使用 RpcClient 发送 RPC 请求
//    	                    }
//    	                })
//    	                .option(ChannelOption.SO_KEEPALIVE, true);
//    	             future = bootstrap.connect(host, port).sync();
//    	             NettyChannelLRUMap.add(keyString, (SocketChannel)future.channel());
//    	             channel=future.channel();
//		}
//    	long innerl=System.currentTimeMillis();
//    	 System.out.println("写入链接时间"+(System.currentTimeMillis()-start)+"ms");  
////    	 for(int i=0;i<100;i++)
////    	 {
//    		 channel.writeAndFlush(request);     		
//        	 synchronized (obj) {
//        	       obj.wait(); 
//        	 } 
//    	// }    	
//    	
//         System.out.println("总运行时间"+(System.currentTimeMillis()-innerl)+"ms");
//         response=new RpcResponse();
//         response.setResult("fjdkf");
//         return response;      
//    }
    public int getReqtimeout() {
		return reqtimeout;
	}

	public void setReqtimeout(int reqtimeout) {
		this.reqtimeout = reqtimeout;
	}
	public void run(Timeout timeout) throws Exception {
		// TODO Auto-generated method stub

        
        ChannelFuture future;
        //bootstrap已经初始化好了，只需要将handler填入就可以了
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                   // ch.pipeline().addLast(handlers);
                    ch.pipeline().addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
                    .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
                    .addLast( new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS))
                    .addLast(RpcClientHandler.this); // 使用 RpcClient 发送 RPC 请求
                }
            });
            future = bootstrap.connect(host,port).sync();
        }
        //future对象
        future.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();

                //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                if (!succeed) {
                    System.out.println("重连失败");
                    f.channel().pipeline().fireChannelInactive();
                }else{
                    System.out.println("重连成功");
                    String keyString=String.valueOf(host+port);
                    NettyChannelLRUMap.add(keyString, (SocketChannel)f.channel());
                }
            }
        });
        
    
	}

	
}
