package com.binaration.usecache;

import java.util.Date;
import com.binaration.cache.MemCacheFactory;


public class cachetest {
	public static void main(String[] args) throws Exception
	{
//		try {
//			MemcachedClient client = new MemcachedClient(AddrUtil.getAddresses());
//			client.set("mytestkey", 3600, "124");
//			System.out.println(client.get("mytestkey").toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		for (int i = 0; i < 1000; i++) {
			MemCacheFactory.INSTANCE.getMemCacheManager().set("cztest",new Integer(123), new Date(3600000));
//		}
		System.out.println(MemCacheFactory.INSTANCE.getMemCacheManager().get("cztest").toString());
		System.exit(0);
	}
	
}
