package com.fjsh.rpc.statistic.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fjsh.rpc.statistic.service.IstatisticFacet;


/** 
* @ClassName: StatisticThread 
* @Description:使用方法：
* 项目中只需要设置自己的统计枚举类型，或者直接在sIstatisticFacet.setFacet中填入对应的统计数据也可。
* 在scfclient处初始化启动		new Thread(new StatisticThread()).start();
* 在需要统计的地方
* private static IstatisticFacet sIstatisticFacet=StatisticThread.getStatisticFacetService();
* 在需要统计的地方写入
* sIstatisticFacet.setFacet(AITagStatusEnums.exceptabtest.getType(),AITagStatusEnums.exceptabtest.getDescription(), end-abtesttime);
sIstatisticFacet.setFacet(AITagStatusEnums.alltime.getType(),AITagStatusEnums.exceptabtest.getDescription(), end-start);
sIstatisticFacet.setFacet(AITagStatusEnums.alltimeavg.getType(),AITagStatusEnums.exceptabtest.getDescription(), 1l);
程序会每隔一分钟在日志中打印总的请求量以及请求的平均耗时等信息
* @author fujiansheng@58ganji.com 
* @date 2017年3月20日 上午10:41:22 
*  
*/
public class StatisticThread implements Runnable {
	private static final Log logger = LogFactory.getLog(StatisticThread.class);
	private static final IstatisticFacet statisticFacetService=new StatisticFacetService();
	
	
	public static IstatisticFacet getStatisticFacetService() {
		return statisticFacetService;
	}

	public void run() {
		// TODO Auto-generated method stub
		for(;;)
		{
			try {
			statisticFacetService.printlog();		
				Thread.sleep(60*1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("printlog error",e);
			}
		}
	}


}
