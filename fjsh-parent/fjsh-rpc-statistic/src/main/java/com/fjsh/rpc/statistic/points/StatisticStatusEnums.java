package com.fjsh.rpc.statistic.points;

public enum StatisticStatusEnums{
	
	alltime("0", "一分钟内请求耗时"),
	alltimeavg("0-1", "总体请求次数"),//特殊处理，专门保留请求次数，做总体请求次数以及评价请求耗时
	cookieNull("1", "cookie为空时服务耗时"),
	cookieAlgoExist("2", "cookie以及算法存在时服务耗时"),
	norequest("3", "算法策略未有统计量时耗时"),
	defaultalgo("4", "返回默认算法情况耗时"),
	firstalgo("5", "返回第一个算法情况耗时");

	StatisticStatusEnums(String type, String description){
		
		this.type = type;
		this.description = description;

	}
	
	private String type;
	private String description;
	
	public String getType(){
		
		return type;
	}
	
	public  String getDescription(){
		
		return description;
	}
	public void setType(String inputype){
		
	this.type = inputype;
		
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	// 获得枚举的状态
	public static StatisticStatusEnums getEnumByType(String type){
		
		for (StatisticStatusEnums status : StatisticStatusEnums.values()) {
			
			if (status.type .equals(type)) {
				return status;
			}
		}
		
		return null;
	}
}