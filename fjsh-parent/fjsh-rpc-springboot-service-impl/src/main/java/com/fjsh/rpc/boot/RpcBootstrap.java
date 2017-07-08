package com.fjsh.rpc.boot;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author fujiansheng@58ganji.com 
 */
public class RpcBootstrap {

    public static void main(String[] arg){
        new ClassPathXmlApplicationContext("classpath:spring.xml");
    }
}
