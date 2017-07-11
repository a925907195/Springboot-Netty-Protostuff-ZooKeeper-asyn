package com.fjsh.rpc.client.utils;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

import com.fjsh.rpc.common.ConcurrentLRUHashMap;
import com.fjsh.rpc.common.RpcResponse;

public class NettyResponseLRUMap {
	//默认最大存储返回的一万个对象，同时基于LRU策略进行数据淘汰
    private static ConcurrentLRUHashMap<String,RpcResponse> map=new ConcurrentLRUHashMap<String, RpcResponse>(10240);
    public static void add(String rpcResponseId,RpcResponse rpcResponse){
        map.put(rpcResponseId,rpcResponse);
    }
    public static RpcResponse get(String rpcResponseId){
       return map.get(rpcResponseId);
    }
    public static void remove(String rpcResponseId){
    	map.remove(rpcResponseId);      
    }

}
