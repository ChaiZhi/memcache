package com.binaration.cache;

import java.io.FileInputStream;
import java.util.Properties;


public enum MemCacheFactory {
	INSTANCE;
	
	private IMemCacheManager client;
	
	private MemCacheFactory(){
	}
	
	public static MemCacheFactory getInstance() {
		return INSTANCE;
	}

	public IMemCacheManager getMemCacheManager(){	
		if(client==null){
			synchronized (MemCacheFactory.class) {
				if (client==null){
					client = createClient(getProperties("server"));
				}
			}
		}
		return client;
	}
	
	public void reset(String serveraddrs){
		if (client != null){
			client.reset(serveraddrs);
		}
	}
	
	private String getProperties(String key)
	{
		Properties Props = new Properties();
		try {
			FileInputStream in = new FileInputStream("application.properties");
			Props.load(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Props.getProperty(key);
	}
	
	protected IMemCacheManager createClient(String servers) {
		IMemCacheManager client = null;
		if (servers.isEmpty()) {
			System.out.println("server addr is empty!");
		} else {
			if(getProperties("client").equals("spymemcached"))
			client = new Spymemclient(servers, true, Integer.valueOf(getProperties("clientnum")));
		}
		return client;
	}
	
}

