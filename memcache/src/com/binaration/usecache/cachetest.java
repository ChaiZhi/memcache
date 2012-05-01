package com.binaration.usecache;

import java.util.Date;

import net.spy.memcached.CASMutation;

import com.binaration.cache.IMemCacheManager;
import com.binaration.cache.MemCacheFactory;

public class cachetest {
	public static void main(String[] args) throws Exception
	{
		String key = "memtest";
		IMemCacheManager client = MemCacheFactory.INSTANCE.getMemCacheManager();
		client.set(key, new Long(123), new Date(3600000));
		System.out.println(key + " : " + client.get("cztest").toString());

		final Long defaultvalue = 1L;
		client.cas(key, defaultvalue, new Date(3600000), new CASMutation<Long>() {
			@Override
			public Long getNewValue(Long oldValue) {
				return oldValue + 1L;
			}
		});
		System.out.println(key + " : " + client.get(key));
		System.exit(0);
	}

}
