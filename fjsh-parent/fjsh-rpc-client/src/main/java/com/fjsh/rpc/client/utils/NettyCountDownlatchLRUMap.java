package com.fjsh.rpc.client.utils;

import java.util.concurrent.CountDownLatch;

import com.fjsh.rpc.common.ConcurrentLRUHashMap;

public class NettyCountDownlatchLRUMap {
	//默认最大存储返回的一万个对象，同时基于LRU策略进行数据淘汰
    private static ConcurrentLRUHashMap<String,CountDownLatch> map=new ConcurrentLRUHashMap<String, CountDownLatch>(10240);
    public static void add(String rpcResponseId,CountDownLatch countDownLatch){
        map.put(rpcResponseId,countDownLatch);
    }
    public static CountDownLatch get(String rpcResponseId){
       return map.get(rpcResponseId);
    }
    public static void remove(String rpcResponseId){
    	map.remove(rpcResponseId);      
    }

}
