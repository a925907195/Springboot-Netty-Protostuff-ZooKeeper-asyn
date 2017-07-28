package com.fjsh.rpc.client;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fjsh.rpc.common.RpcRequest;
import com.fjsh.rpc.common.RpcResponse;
import com.fjsh.rpc.connection.utils.AskMsg;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;

public class RpcProxy {

	private String serverAddress;  
	//保存不同服务信息对应的ServiceDiscovery对象
    private static ConcurrentHashMap<String, ServiceDiscovery> serviceDisConcurrentHashMap=new ConcurrentHashMap<String, ServiceDiscovery>();
    private String registryAddress;
    private int zk_session_timeout;
    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(String registryAddress,
			int zk_session_timeout) {
		super();
		this.registryAddress = registryAddress;
		this.zk_session_timeout = zk_session_timeout;
	}

//	public RpcProxy(ServiceDiscovery serviceDiscovery) {
//        this.serviceDiscovery = serviceDiscovery;
//    }

    /**
     * @param zk_registry_path 服务对应的注册中心的名称
     * @param interfaceClass 调用的服务类名称
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final String zk_registry_path,Class<?> interfaceClass) {
    	final ServiceDiscovery serviceDiscovery;
    	if(serviceDisConcurrentHashMap.containsKey(zk_registry_path))
    	{
    		serviceDiscovery=serviceDisConcurrentHashMap.get(zk_registry_path);
    	}
    	else {
    		serviceDiscovery=new ServiceDiscovery(registryAddress, zk_session_timeout, "/"+zk_registry_path);
    		serviceDisConcurrentHashMap.put(zk_registry_path, serviceDiscovery);
		}
    	        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(args);                   
                    if (serviceDiscovery != null) {
                        serverAddress = serviceDiscovery.discover(); // 发现服务
                    }
                    request.setBaseMsg(new AskMsg());
                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    RpcClient client = new RpcClient(host, port); // 初始化 RPC 客户端
                    RpcResponse response = client.send(request); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应

                    if (response.getError() != null) {
                        throw response.getError();
                    } else {
                        return response.getResult();
                    }
                }
            }
        );
    }
}
