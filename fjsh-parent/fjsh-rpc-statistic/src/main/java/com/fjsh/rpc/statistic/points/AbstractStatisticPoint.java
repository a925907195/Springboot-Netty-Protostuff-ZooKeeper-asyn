package com.fjsh.rpc.statistic.points;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractStatisticPoint {
	public static final Map<String, PointEntry> StaticPoint=new ConcurrentHashMap<String, PointEntry>();
}
