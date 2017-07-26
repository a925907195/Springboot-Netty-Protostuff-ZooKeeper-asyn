package com.fjsh.rpc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fjsh.rpc.client.RpcProxy;
import com.fjsh.rpc.service.IHelloService;
import com.fjsh.rpc.statistic.points.StatisticStatusEnums;
import com.fjsh.rpc.statistic.service.IstatisticFacet;
import com.fjsh.rpc.statistic.service.impl.StatisticThread;

@Controller
public class HelloController {
	IstatisticFacet sIstatisticFacet=StatisticThread.getStatisticFacetService();
	@Autowired
	private RpcProxy rpcProxy;
	
	//http://localhost:8080/fjsh-rpc-client/hello?name=fjsdfhd324
	@RequestMapping("/hello")
	public void hello(String name){	
		 IHelloService service = rpcProxy.create("registrytemp",IHelloService.class);
		new Thread(new StatisticThread()).start();	
//		for(int i=0;i<1000;i++)
//		{
			long start=System.currentTimeMillis();
			service.hello(name);
			sIstatisticFacet.setFacet(StatisticStatusEnums.alltimeavg.getType(),StatisticStatusEnums.alltimeavg.getDescription(), 1l);
			sIstatisticFacet.setFacet(StatisticStatusEnums.alltime.getType(), StatisticStatusEnums.alltime.getDescription(), System.currentTimeMillis()-start);
//		}
	}
	
//	@RequestMapping("/getUser")
//	public void getUser(String name){
//		System.out.println(service.getUser(name).toString());
//	}
//	
//	@RequestMapping("/getUsers")
//	public void getUsers(int size){
//		List<User> list = service.getUsers(size);
//		for(User user : list){
//			System.out.println(user.toString());
//		}
//	}
//	
//	@RequestMapping("/updateUser")
//	public void updateUser(String name){
//		User user = new User(name, new Date(), true);
//		user = service.updateUser(user);
//		System.out.println(user.toString());
//	}
}
