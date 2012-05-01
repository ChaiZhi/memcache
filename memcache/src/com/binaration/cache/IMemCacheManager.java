package com.binaration.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.spy.memcached.CASMutation;

/**
 * 缓存处理接口
 * 
 * @author zhi.chai
 * 
 */
public interface IMemCacheManager {

	/**
	 * 添加一个缓存键值对，如果键已经存在则返回false
	 * @param key		缓存的键
	 * @param value		缓存的值
	 * @param expiry	过期时间的 “秒数”
	 * @return			操作是否成功
	 */
	public abstract boolean add(String key, Object value, Date expiry);

	/**
	 * 强制设置一个键的值，即使这个键已经存在
	 * @param key		缓存的键
	 * @param value		缓存的值
	 * @param expiry	过期时间的 “秒数”
	 * @return			操作是否成功
	 */
	public abstract boolean set(String key, Object value, Date expiry);

	/**
	 * 替换已有的键的值，必须在这个键已经存在的情况下才生效
	 * @param key		缓存的键
	 * @param value 	缓存的值
	 * @param expiry 	过期时间的 “秒数”
	 * @return 			操作是否成功
	 */
	public abstract boolean replace(String key, Object value, Date expiry);

	/**
	 * 删除对应键的缓存
	 * @param key 		缓存的键
	 * @return 			操作是否成功
	 */
	public abstract boolean delete(String key);

	/**
	 * 取得相应键的值
	 * @param key 		缓存的键
	 * @return 			缓存的值对象
	 */
	public abstract Object get(String key);

	/**
	 * 对给定的键的值增加1(一般用于计数器)
	 * @param key 		缓存的键
	 * @return 			增加过的相应的值
	 */
	public abstract long incr(String key);

	/**
	 * 对给定的键的值增加给定的增加量
	 * @param key 		缓存的键
	 * @param inc 		给定的增量
	 * @return 			增加过的相应的值
	 */
	public abstract long incr(String key, int inc);

	/**
	 * 获取自增id，不存在时，1为默认值 原子操作
	 * 
	 * @param key 		缓存的键
	 * @return 			增加过的相应的值
	 */
	public abstract long getAutoIncrementId(String key);

	/**
	 * 对给定的键的值减少1(一般用于计数器)
	 * @param key 		缓存的键
	 * @return 			减少过的相应的值
	 */
	public abstract long decr(String key);

	/**
	 * 对给定的键的值减少给定的减少量
	 * @param key 		缓存的键
	 * @param desr		给定的减少量
	 * @return 			减少过的相应的值
	 */
	public abstract long decr(String key, int decr);

	/**
	 * 将服务器地址重设为新的给定的地址
	 * @param serveraddrs	新的地址
	 */
	public abstract void reset(String serveraddrs);

	/**
	 * 获取memcache中的多个对象，返回结果为List格式
	 * @param keys 		key列表
	 * @return 			未过滤结果中的null对象
	 */
	public abstract <T> List<T> getArrays(Collection<String> keys);

	/**
	 * 获取memcache中的多个对象，返回结果为List格式
	 * @param keys 		key列表
	 * @return 			过滤结果中的null对象
	 */
	public abstract <T> List<T> getArraysWithNull(Collection<String> keys);

	/**
	 * 获取memcache中的多个对象，返回结果为Map格式
	 * @param keys 		key列表
	 * @return 			过滤结果中的null对象
	 */
	public abstract Map<String, Object> getBulk(Collection<String> keys);

	/**
	 * 获取memcache中的多个对象，返回结果为Map格式
	 * @param keys 		key列表
	 * @return 			未过滤结果中的null对象
	 */
	public abstract Map<String, Object> getBulkWithNull(Collection<String> keys);

	/**
	 * check and set 检查并设置，如果已经有相应键，则通过提过的mutation完成对值的更新操作
	 * @param <T> 		value对象的类型
	 * @param key 		需要更新的key值
	 * @param initial 	原value为null时，需要add的对象
	 * @param date 		过期时间
	 * @param mutation 	通过原value对象生成新value对象的接口
	 * @return
	 */
	public abstract <T> T cas(String key, T initial, Date date, CASMutation<T> mutation);
}