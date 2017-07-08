package com.fjsh.rpc.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fjsh.rpc.common.Constant;

public class ServiceRegistry {

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceRegistry.class);

	private CountDownLatch latch = new CountDownLatch(1);
	 private static ZooKeeper zk = null;  
	private String registryAddress;
	 private Object waiter = new Object(); 
	 private String registrydata;//当前服务节点在zk上的注册数据
	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public void register(String data) {
		if (data != null) {
			registrydata=data;
			 zk = connectServer();
			if (zk != null) {
				createNode(zk, data);
			}
		}
	}

	private ZooKeeper connectServer() {
		if( zk == null||zk.getState()==States.CLOSED)
		{
			try {
				 SessionWatcher watcher = new SessionWatcher(); 
				zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,watcher);
				latch.await();
			} catch (IOException e) {
				logger.error("", e);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
		
		return zk;
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		try {
			if (zk != null) {
				zk.close();
			}
		} catch (InterruptedException e) {
			logger.error("release connection error ," + e.getMessage(), e);
		}
	}

	private void createNode(ZooKeeper zk, String data) {
		try {
			byte[] bytes = data.getBytes();
			// 判断目录是否存在，如果不存在则进行创建
			if (zk.exists(Constant.ZK_REGISTRY_PATH, false) == null) {
				zk.create(Constant.ZK_REGISTRY_PATH, "".getBytes(),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String path = zk.create(Constant.ZK_DATA_PATH, bytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			logger.debug("create zookeeper node ({} => {})", path, data);
		} catch (KeeperException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}
	 class SessionWatcher implements Watcher {  
		  
	        public void process(WatchedEvent event) {  
	            // 如果是“数据变更”事件    
	            if (event.getType() != Event.EventType.None) {    
	                return;    
	            }   
	            synchronized (waiter){  
	                switch(event.getState()) {  
	                case SyncConnected:
	        			// zk连接建立成功,或者重连成功
	        			latch.countDown();
	        			logger.info("Connected...");
	        			break;
	        		case Expired:
	        			// session过期,这是个非常严重的问题,有可能client端出现了问题,也有可能zk环境故障
	        			// 此处仅仅是重新实例化zk client
	        			logger.info("Expired(重连)并重新进行数据在zk上的注册...");
	        			register(registrydata);
	        			break;
	                    case Disconnected: 
	                        logger.info("链接断开，或session迁移，重试链接并链接成功后重新注册数据...."); 
	                        register(registrydata);
	                        break;  
	                    case AuthFailed:  
	                        close();    
	                        throw new RuntimeException("ZK Connection auth failed...");  
	                    default:  
	                        break;  
	                }  
	            }  
	        }  
	    }  
}
