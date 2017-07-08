package com.fjsh.rpc.service;

import java.util.List;

import com.fjsh.rpc.entity.User;

public interface IHelloService {

	public String hello(String name);
	
	public User getUser(String name);
	
	public List<User> getUsers(int size);
	
	public User updateUser(User user);
	
}
