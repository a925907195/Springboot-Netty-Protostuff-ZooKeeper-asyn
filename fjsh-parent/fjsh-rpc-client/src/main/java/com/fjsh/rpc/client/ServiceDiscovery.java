package com.fjsh.rpc.client;

import io.netty.util.internal.ThreadLocalRandom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fjsh.rpc.common.Constant;

public class ServiceDiscovery {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
	private Object waiter = new Object(); 
    private CountDownLatch latch = new CountDownLatch(1);
    private volatile List<String> dataList = new ArrayList<String>();
    private static ZooKeeper zk = null;  
    private String registryAddress;
    private int zk_session_timeout;
    private String zk_registry_path;
    public ServiceDiscovery(String registryAddress,int zk_session_timeout,String zk_registry_path) {
        this.registryAddress = registryAddress;
        this.zk_session_timeout=zk_session_timeout;
        this.zk_registry_path=zk_registry_path;
         zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }
    public ServiceDiscovery(String registryAddress,int zk_session_timeout) {
        this.registryAddress = registryAddress;
        this.zk_session_timeout=zk_session_timeout;
         zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }
    public String getZk_registry_path() {
		return zk_registry_path;
	}

	public void setZk_registry_path(String zk_registry_path) {
		this.zk_registry_path = zk_registry_path;
	}

	/**
     * 如果出现链接断开，session超时等情况，重新链接zk并重新对数据信息进行监听
     */
    public void reServiceDiscovery()
    {
    	  zk = connectServer();
          if (zk != null&&zk.getState()==States.CONNECTED) {
              watchNode(zk);
          }
    }
    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                logger.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                logger.debug("using random data: {}", data);
            }
        }
        return data;
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
     * 不断循环监听zk信息变化，默认监听只监听一次，通过不断循环设置监听
     * 具体的zk watch策略可以参考下面
     * http://blog.csdn.net/a925907195/article/details/73772593     * 
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
        	//动态实时监听zk上数据变化
            List<String> nodeList = zk.getChildren(zk_registry_path, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(zk_registry_path + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            logger.debug("node data: {}", dataList);
            this.dataList = dataList;
        } catch (KeeperException e) {
        	e.printStackTrace();
            logger.error("", e);
        } catch (InterruptedException e){
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
        			logger.info("Expired(重连)并重新监听数据...");
        			reServiceDiscovery();
        			break;
                    case Disconnected: 
                        logger.info("链接断开，或session迁移，重试链接并重新监听数据...."); 
                        reServiceDiscovery();
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
