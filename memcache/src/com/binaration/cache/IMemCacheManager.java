package com.binaration.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 缓存处理接口
 * @author zhi.chai
 *
 */
public interface IMemCacheManager {

	public abstract boolean add(String key, Object value, Date date);

	public abstract boolean set(String key, Object value, Date expiry);

	public abstract boolean replace(String key, Object value, Date expiry);

	public abstract boolean delete(String key);

	public abstract Object get(String key);

	public abstract long incr(String key, int inc);

	public abstract long incr(String key);
	
	public abstract long getAutoIncrementId(String key);

	public abstract long decr(String key);

	public abstract long decr(String key, int inc);
	
	public abstract void reset(String serveraddrs);
	
	public abstract <T> List<T> getArrays(Collection<String> keys);
	
	public abstract <T> List<T> getArraysWithNull(Collection<String> keys);
	
	public abstract Map<String, Object> getBulk(Collection<String> keys);
	
	public abstract Map<String, Object> getBulkWithNull(Collection<String> keys);
	
	public abstract <T> T cas(String key, T initial, Date date, EasyCASMutation<T> mutation);

}