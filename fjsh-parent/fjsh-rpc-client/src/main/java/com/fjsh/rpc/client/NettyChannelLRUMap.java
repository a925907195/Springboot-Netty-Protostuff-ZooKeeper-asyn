package com.fjsh.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

import com.fjsh.rpc.common.ConcurrentLRUHashMap;

public class NettyChannelLRUMap {
    private static ConcurrentLRUHashMap<String,SocketChannel> map=new ConcurrentLRUHashMap<String, SocketChannel>(1024);
    public static void add(String clientId,SocketChannel socketChannel){
        map.put(clientId,socketChannel);
    }
    public static Channel get(String clientId){
       return map.get(clientId);
    }
    public static void remove(SocketChannel socketChannel){
        for (Map.Entry entry:map.entrySet()){
            if (entry.getValue()==socketChannel){
                map.remove(entry.getKey());
            }
        }
    }

}
