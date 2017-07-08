package com.fjsh.rpc.statistic.points;

/**
 * @author Administrator
 *用于统计观察节点
 */
public class PointEntry {
	private String type;
	private String description;
	
	public PointEntry(String type, String description) {
		super();
		this.type = type;
		this.description = description;
	}

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
}
