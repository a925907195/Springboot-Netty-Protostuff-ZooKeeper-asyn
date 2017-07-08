package com.fjsh.rpc.common;

import org.springframework.stereotype.Component;

@Component
public class Constant {
	//zk seesion过期时间
	public static int ZK_SESSION_TIMEOUT = 5000;

	// 在创建数据节点前，先用zkCli.sh客户端连接上服务端，查看目前存在的数据节点，
	// #服务启动后服务在zk上的注册目录地址,此目录持久保持，如果没有则自动创建
	public static String ZK_REGISTRY_PATH;
	// #服务启动后在对应的注册服务目录下面注册对应的服务提供节点，此节点为临时节点session过期后自动删除并且序列号自增
	public static String ZK_DATA_PATH;

	public int getZK_SESSION_TIMEOUT() {
		return ZK_SESSION_TIMEOUT;
	}
	public void setZK_SESSION_TIMEOUT(int zK_SESSION_TIMEOUT) {
		ZK_SESSION_TIMEOUT = zK_SESSION_TIMEOUT;
	}

	public String getZK_REGISTRY_PATH() {
		return ZK_REGISTRY_PATH;
	}
	public void setZK_REGISTRY_PATH(String zK_REGISTRY_PATH) {
		ZK_REGISTRY_PATH = zK_REGISTRY_PATH;
	}

	public String getZK_DATA_PATH() {
		return ZK_DATA_PATH;
	}
	public void setZK_DATA_PATH(String zK_DATA_PATH) {
		ZK_DATA_PATH = zK_DATA_PATH;
	}

}
