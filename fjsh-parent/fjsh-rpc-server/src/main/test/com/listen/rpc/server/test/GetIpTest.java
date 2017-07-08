package com.listen.rpc.server.test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetIpTest {
	public static void main(String[] args) throws Exception {
		System.out.println(getip());
	}
	
	/**
	 *获取本机ip地址
	 * @throws Exception
	 */
	public static String getip() throws Exception
	{
		Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			//System.out.println(netInterface.getName());
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					if(ip.getHostAddress().startsWith("127")||(ip.getHostAddress().startsWith("192")&&ip.getHostAddress().lastIndexOf(".1")!=-1))
					{
						continue;
					}
					return ip.getHostAddress();
				}
			}
		}
		return "127.0.0.1";//默认返回本机回送地址
	}
}
