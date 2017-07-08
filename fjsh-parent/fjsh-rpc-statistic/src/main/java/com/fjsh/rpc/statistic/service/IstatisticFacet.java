package com.fjsh.rpc.statistic.service;

/**
 * @author Administrator
 *项目时间性能统计代码，用来依赖以后进行性能的监控
 */
public interface IstatisticFacet {
	public abstract void setFacet(String point,String desc,long times);
	public abstract void printlog();
	public abstract void clean();
}
