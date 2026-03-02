package io.renren;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import io.renren.common.utils.IpUtils;

public class IpTest {

	
	
	
	public static void main(String[] args) throws Exception {
		
		List<String> lists = Lists.newArrayList();
		lists.add("105.165.95.142");
		lists.add("81.152.185.24");
		lists.add("1.38.106.226");
		lists.add("110.74.206.253");
		lists.add("36.37.204.200");
		lists.add("38.90.17.6");
		lists.add("103.110.66.195");
		lists.add("153.67.65.93");
		lists.add("51.34.98.65");
		
		for(String ip : lists) {
			System.out.println(IpUtils.getCity(ip));
			Thread.sleep(2000);
		}
	}
}
