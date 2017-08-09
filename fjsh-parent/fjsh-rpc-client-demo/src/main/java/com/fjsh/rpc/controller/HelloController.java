package com.fjsh.rpc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fjsh.rpc.client.RpcProxy;
import com.fjsh.rpc.entity.User;
import com.fjsh.rpc.service.IHelloService;
import com.fjsh.rpc.statistic.points.StatisticStatusEnums;
import com.fjsh.rpc.statistic.service.IstatisticFacet;
import com.fjsh.rpc.statistic.service.impl.StatisticThread;

@Controller
public class HelloController {
	IstatisticFacet sIstatisticFacet=StatisticThread.getStatisticFacetService();
	@Autowired
	private RpcProxy rpcProxy;	
	//http://localhost:8090/fjsh-rpc-client/hello?name=fjsdfhd324
	@ResponseBody
	@RequestMapping("/hello")
	public String hello(String name){
		 IHelloService service = rpcProxy.create("registrytemp",IHelloService.class);
		//new Thread(new StatisticThread()).start();
			//long start=System.currentTimeMillis();			
//			sIstatisticFacet.setFacet(StatisticStatusEnums.alltimeavg.getType(),StatisticStatusEnums.alltimeavg.getDescription(), 1l);
//			sIstatisticFacet.setFacet(StatisticStatusEnums.alltime.getType(), StatisticStatusEnums.alltime.getDescription(), System.currentTimeMillis()-start);
		 String retstr=service.hello(name);
		 return "通过rpc调用返回的字符串为: "+retstr;
	}
	
//	http://localhost:8090/fjsh-rpc-client/info?username=fjsdfhd3245
	/**
	 * velocity test
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/info")
	public String getInfo(HttpServletRequest request,
			HttpServletResponse response) {
		String username = request.getParameter("username");
		return "fjsh web";
	}
	
	/**
	 * http://localhost:8090/fjsh-rpc-client/getuser?username=fjsh
	 * 根据用户名返回用户信息
	 * @param modelMap
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/getuser")
	public String getUser(ModelMap modelMap,HttpServletRequest request,
			HttpServletResponse response){
		IHelloService service = rpcProxy.create("registrytemp",IHelloService.class);
		String username = request.getParameter("username");
		
		User usertemp=service.getUser(username);
		modelMap.put("user", usertemp);
		return "userinfo";
	}
}
