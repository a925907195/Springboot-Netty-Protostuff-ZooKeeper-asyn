package com.fjsh.rpc.statistic.service.impl;

import org.junit.Test;

import com.fjsh.rpc.statistic.points.StatisticStatusEnums;
import com.fjsh.rpc.statistic.service.IstatisticFacet;

public class StatisticThreadTest {
	IstatisticFacet sIstatisticFacet=StatisticThread.getStatisticFacetService();
	@Test
	public void testGetStatisticFacetService() {
		new Thread(new StatisticThread()).start();
		
		for(int i=0;i<200;i++)
		{
			long start=System.currentTimeMillis();
			int temp=i;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sIstatisticFacet.setFacet(StatisticStatusEnums.alltimeavg.getType(),StatisticStatusEnums.alltimeavg.getDescription(), 1l);
			sIstatisticFacet.setFacet(StatisticStatusEnums.alltime.getType(), StatisticStatusEnums.alltime.getDescription(), System.currentTimeMillis()-start);
			
		}
		
	}

}
