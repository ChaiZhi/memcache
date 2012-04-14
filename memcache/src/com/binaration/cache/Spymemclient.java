package com.binaration.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.CASMutator;
import net.spy.memcached.KetamaConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

public class Spymemclient implements IMemCacheManager {
	private static final int DEFAULT_STEP = 1;

	private List<MemcachedClient> clients;
	private String serveraddrs;
	private boolean useConsistantHash;

	private int timeout = 5;
	private Random rand=new Random(System.currentTimeMillis());
	
//	private static final Logger logger=Logger.getLogger("com.binaration.cache.EasyCacheClient");
	
	private MemcachedClient getClient() {
		if (clients == null){
			synchronized (Spymemclient.class){
				if (clients == null){
					clients=new ArrayList<MemcachedClient>();
					try {
						for(int i=0;i<5;i++){
							if(useConsistantHash){
								clients.add(new MemcachedClient(new KetamaConnectionFactory(), AddrUtil.getAddresses(serveraddrs)));
							}else{
								clients.add(new MemcachedClient(AddrUtil.getAddresses(serveraddrs)));
							}
						}
					} catch (Exception e) {
						System.out.println("init cache client failed! svrs="+serveraddrs);
					}
				}
			}
		}
		return clients.get(rand.nextInt(clients.size()));
	}
	
	/**
	 * not use consistant hash
	 * @param serveraddrs
	 */
	public Spymemclient(String serveraddrs) {
		this(serveraddrs,false);
	}
	
	
	public Spymemclient(String serveraddrs,boolean useConsistantHash) {
		this.serveraddrs = serveraddrs;
		this.useConsistantHash = useConsistantHash;
	}
	
	public void reset(String serveraddrs){
		this.serveraddrs = serveraddrs;
		this.clients = null;
	}
	
	public boolean add(String key, Object value, Date date) {
		return futureBoolean(getClient().add(key,(int) (date.getTime() / 1000L), value));
	}

	public boolean set(String key, Object value, Date expiry) {
		return futureBoolean( getClient().set(key, (int) (expiry.getTime() / 1000L), value));
	}

	public boolean replace(String key, Object value, Date expiry) {
		return futureBoolean(getClient().replace(key,(int) (expiry.getTime() / 1000L), value));
	}

	public boolean delete(String key) {
		return futureBoolean(getClient().delete(key));
	}

	public Object get(String key) {
		return futureObject(getClient().asyncGet(key));
	}
	
	public long incr(String key, int inc) {
		return getClient().incr(key, inc);
	}
	public long incr(String key) {
		return getClient().incr(key, DEFAULT_STEP);
	}

	public long decr(String key) {
		return getClient().decr(key, DEFAULT_STEP);
	}

	public long decr(String key, int inc) {
		return getClient().decr(key, inc);
	}
	
	/**
	 * 获取自增id，不存在时，1为默认值 原子操作
	 * 
	 * @param key 自增变量key值
	 * @return
	 */
	public long getAutoIncrementId(String key) {
		return getClient().incr(key, DEFAULT_STEP, DEFAULT_STEP);
	}
	
	/**
	 * 通过mget获取memcache中的多个对象，返回结果为Map格式
	 * @param keys key列表
	 * @return 过滤结果中的null对象
	 */
	public Map<String, Object> getBulk(Collection<String> keys) {
		Map<String, Object> bulkMap = new HashMap<String, Object>((int)(keys.size() * 1.5D));
		Map<String, Object> resultMap = getBulkWithNull(keys);
		for (Entry<String, Object> entry : resultMap.entrySet()) {
			if (null != entry.getValue())
				bulkMap.put(entry.getKey(), entry.getValue());
		}
		return bulkMap;
	}
	
	/**
	 * 通过mget获取memcache中的多个对象，返回结果为Map格式
	 * @param keys key列表
	 * @return 未过滤结果中的null对象
	 */
	public Map<String, Object> getBulkWithNull(Collection<String> keys) {
		if ((keys == null) || (keys.isEmpty())) {
			return null;
		}
		if (keys.size() < 255) {
			return futureMap(getClient().asyncGetBulk(keys));
		}
		Map<String, Object> result = new HashMap<String, Object>((int)(keys.size() * 1.5D));
		for (String[] keyArray : cutKeys(keys)) {
			Map<String, Object> subResult = futureMap(getClient().asyncGetBulk(Arrays.asList(keyArray)));
			result.putAll(subResult);
		}
		return result;
	}
	
	/**
	 * 通过mget获取memcache中的多个对象，返回结果为List格式
	 * @param keys key列表
	 * @return 未过滤结果中的null对象
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getArrays(Collection<String> keys){
		List<T> resultList = new ArrayList<T>(keys.size());
		Map<String, Object> resultMap = getBulkWithNull(keys);
		for (Entry<String, Object> entry : resultMap.entrySet()) {
			if (null != entry.getValue())
				resultList.add((T)entry.getValue());
		}
		return resultList;
	}
	
	/**
	 * 通过mget获取memcache中的多个对象，返回结果为List格式
	 * @param keys key列表
	 * @return 过滤结果中的null对象
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getArraysWithNull(Collection<String> keys){
		List<T> resultList = new ArrayList<T>(keys.size());
		Map<String, Object> resultMap = getBulkWithNull(keys);
		for (Entry<String, Object> entry : resultMap.entrySet()) {
			resultList.add((T)entry.getValue());
		}
		return resultList;
	}
	
	/**
	 * 
	 * 
	 * @param <T> value对象的类型
	 * @param key 需要更新的key值
	 * @param initial 原value为null时，需要add的对象
	 * @param date 过期时间
	 * @param mutation 通过原value对象生成新value对象的接口
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T cas(String key, T initial, Date date, EasyCASMutation<T> mutation){
		MemcachedClient client = getClient();
		CASMutator<T> mutator = new CASMutator<T>(client, (Transcoder<T>)client.getTranscoder(), 10); 
		try {
			return mutator.cas(key, initial, (int) (date.getTime() / 1000L), mutation);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	protected boolean futureBoolean(Future<Boolean> f){
		boolean success = false;
		try {
			success = f.get(this.timeout, TimeUnit.SECONDS);
			return success;
		} catch (Exception e) {
			f.cancel(false);
		}
		return false;
	}
	
	protected Object futureObject(Future<Object> f){
		Object myObj = null;
		try {
			myObj = f.get(this.timeout, TimeUnit.SECONDS);
			return myObj;
		} catch (Exception e) {
			f.cancel(false);
		}
		return null;
	}
	
	protected Map<String, Object> futureMap(Future<Map<String, Object>> f){
		Map<String, Object> myMap = null;
		try {
			myMap = f.get(this.timeout, TimeUnit.SECONDS);
			return myMap;
		} catch (Exception e) {
			f.cancel(false);
		}
		return null;
	}
	
	protected Collection<String[]> cutKeys(Collection<String> keys) {
		List<String[]> result = new ArrayList<String[]>(keys.size() / 255 + 1);
		String[] keysArray = (String[]) keys.toArray(new String[keys.size()]);
		int start = 0;
		int end = 255;
		while (start < keysArray.length) {
			end = (end > keysArray.length) ? keysArray.length : end;
			int size = end - start;
			String[] subKeys = new String[size];
			System.arraycopy(keysArray, start, subKeys, 0, size);
			result.add(subKeys);
			start += 255;
			end += 255;
		}
		return result;
	}
	
	public static void main(String[] argv){
		System.setProperty("configInfoFilePath", "./web/WEB-INF/config-manager-info.xml");
		System.setProperty("configManagerDebug", "true");
		
		
		String key = "a";
		final String value = "bb";
		IMemCacheManager client = MemCacheFactory.getInstance().getMemCacheManager();
		client.cas(key, value, new Date(3600), new EasyCASMutation<String>(){
			@Override
			public String getNewValue(String oldValue) {
				return oldValue + "," + value;
			}
		});
		System.out.println(client.get(key));
	}

}