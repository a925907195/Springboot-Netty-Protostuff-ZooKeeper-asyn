package com.fjsh.rpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import com.fjsh.rpc.common.RpcRequest;
import com.fjsh.rpc.common.RpcResponse;
import com.fjsh.rpc.connection.utils.AskMsg;
import com.fjsh.rpc.connection.utils.BaseMsg;
import com.fjsh.rpc.connection.utils.LoginMsg;
import com.fjsh.rpc.connection.utils.MsgType;
import com.fjsh.rpc.connection.utils.PingMsg;
import com.fjsh.rpc.connection.utils.ReplyClientBody;
import com.fjsh.rpc.connection.utils.ReplyMsg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	/**
	 * 为了避免使用 Java 反射带来的性能问题，我们可以使用 CGLib 提供的反射 API，如上面用到的FastClass与FastMethod。
	 */
	private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
	 private int loss_connect_time = 0;
    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {       
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                System.out.println("10 秒没有接收到客户端的信息了");
                if(loss_connect_time > 3 ){
                	NettyChannelLRUMap.remove((SocketChannel)ctx.channel());
                    ctx.channel().close();
                }
            }  
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelLRUMap.remove((SocketChannel)ctx.channel());
    }
    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
    	 RpcResponse response = new RpcResponse();
         response.setRequestId(request.getRequestId());
    	BaseMsg baseMsg=request.getBaseMsg();
    	 NettyChannelLRUMap.add(String.valueOf(channelHandlerContext.channel().id()),(SocketChannel)channelHandlerContext.channel());
        if(MsgType.LOGIN.equals(baseMsg.getType())){
          /*  LoginMsg loginMsg=(LoginMsg)baseMsg;
            if("robin".equals(loginMsg.getUserName())&&"yao".equals(loginMsg.getPassword())){
                //登录成功,把channel存到服务端的map中
                NettyChannelLRUMap.add(loginMsg.getClientId(),(SocketChannel)channelHandlerContext.channel());
                logger.debug("client"+loginMsg.getClientId()+" 登录成功");
            }*/
        }else{
            /*if(NettyChannelLRUMap.get(baseMsg.getClientId())==null){
                    //说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录
                    LoginMsg loginMsg=new LoginMsg();
                    channelHandlerContext.channel().writeAndFlush(loginMsg);
            }*/
        }
        switch (baseMsg.getType()){
            case PING:{
                PingMsg pingMsg=(PingMsg)baseMsg;
                PingMsg replyPing=new PingMsg();
                System.out.println("receive ping message……");
              //  NettyChannelLRUMap.get(String.valueOf(channelHandlerContext.channel().id())).writeAndFlush(replyPing);
            }break;
            case ASK:{
                //收到客户端的请求
                 try {
                     Object result = handle(request);
                     response.setResult(result);
                     response.setBaseMsg(new ReplyMsg());
                 } catch (Throwable t) {
                     response.setError(t);
                 }
                Channel channel= NettyChannelLRUMap.get(String.valueOf(channelHandlerContext.channel().id()));  
                System.out.println(channel.isActive());
                channel.writeAndFlush(response);
            }break;
            case REPLY:{
                //收到客户端回复
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyClientBody clientBody=(ReplyClientBody)replyMsg.getBody();
                logger.debug("receive client msg: "+clientBody.getClientInfo());
            }break;
            default:break;
        }
        ReferenceCountUtil.release(request);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        NettyChannelLRUMap.remove((SocketChannel)ctx.channel());
        ctx.close();
    }

}
