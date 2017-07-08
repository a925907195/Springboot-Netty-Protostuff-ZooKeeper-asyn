package com.fjsh.rpc.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fjsh.rpc.annotation.RpcService;
import com.fjsh.rpc.entity.User;
import com.fjsh.rpc.service.IHelloService;

@RpcService(IHelloService.class)// 指定远程接口  使用 RpcService注解定义在服务接口的实现类上，需要对该实现类指定远程接口，因为实现类可能会实现多个接口，一定要告诉框架哪个才是远程接口。
public class HelloServiceImpl implements IHelloService {

	public String hello(String name) {
		String result = "hello" + name;
		System.out.println(result);
		return result;
	}

//	@Override
	public User getUser(String name) {
		User user = new User(name, new Date(), true);
		return user;
	}

//	@Override
	public List<User> getUsers(int size) {
		List<User> list = new ArrayList<User>();
		User user = null;
		String name = "foo";
		Date birthday = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(birthday);
		for(int i = 0; i < size; i++){
			cal.add(Calendar.DAY_OF_MONTH, 1);
			user = new User(name, cal.getTime(), i%2==0 ? true : false);
			list.add(user);
		}
		return list;
	}

//	@Override
	public User updateUser(User user) {
		user.setName(user.getName() + "-update");
		return user;
	}

}
