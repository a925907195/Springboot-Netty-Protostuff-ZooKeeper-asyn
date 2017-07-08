package com.fjsh.rpc.statistic.service.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fjsh.rpc.statistic.points.AbstractStatisticPoint;
import com.fjsh.rpc.statistic.points.PointEntry;
import com.fjsh.rpc.statistic.points.StatisticStatusEnums;
import com.fjsh.rpc.statistic.service.IstatisticFacet;

public class StatisticFacetService implements IstatisticFacet {
	private ConcurrentHashMap<String, Long> facetConcurrent=new ConcurrentHashMap<String, Long>();
	private static final Log logger = LogFactory.getLog(StatisticFacetService.class);
	public ConcurrentHashMap<String, Long> getFacetConcurrent() {
		return facetConcurrent;
	}

	public void setFacetConcurrent(ConcurrentHashMap<String, Long> facetConcurrent) {
		this.facetConcurrent = facetConcurrent;
	}
	public void setFacet(String point,String desc, long times) {
		// TODO Auto-generated method stub
		if(null==point||point.trim().equals(""))
		{
			return ;
		}
		if(!facetConcurrent.contains(point))
		{
			PointEntry pointEntry=new PointEntry(point, desc);
			AbstractStatisticPoint.StaticPoint.put(point, pointEntry);
		}		
		Long temp=facetConcurrent.get(point);
		temp=temp==null?0:temp;
		facetConcurrent.put(point, temp+times);
	}

	public void printlog() {
		// TODO Auto-generated method stub
		StringBuilder sBuffer=new StringBuilder();
		//进行平均值处理
		Long count=facetConcurrent.get(StatisticStatusEnums.alltimeavg.getType());
		count=count==null?1:count;	
		Iterator<java.util.Map.Entry<String, PointEntry>> pointIterable= AbstractStatisticPoint.StaticPoint.entrySet().iterator();
       for (java.util.Map.Entry<String, PointEntry> entry:AbstractStatisticPoint.StaticPoint.entrySet()) { 
    	   if(null!=facetConcurrent.get(entry.getKey()))
    	   {
    		   if(entry.getKey().equals("0-1"))
    		   {
    			   //单独统计次数
        		   sBuffer.append(entry.getValue().getDescription()+":"+facetConcurrent.get(entry.getValue().getType())+"次  ");
        		   continue;
    		   }    		   
    		   sBuffer.append(entry.getValue().getDescription()+":"+facetConcurrent.get(entry.getValue().getType())+"ms  "+entry.getValue().getDescription()+"(平均耗时):"+facetConcurrent.get(entry.getValue().getType())/count+"ms  ");
    	   }			
		}
       logger.info(sBuffer.toString());
       facetConcurrent.clear();
       sBuffer=null;
	}

	public void clean() {
		// TODO Auto-generated method stub
		facetConcurrent.clear();
	}	
}
