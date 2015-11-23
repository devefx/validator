package org.devefx.mirror.test;

import org.devefx.mirror.beans.factory.InitializingBean;
import org.devefx.mirror.cache.DatabaseCache;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisCache implements DatabaseCache, InitializingBean {
	
	private JedisPool jedisPool;
	private JedisPoolConfig config;
	private String host;
	private int port;
	public void setConfig(JedisPoolConfig config) {
		this.config = config;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		jedisPool = new JedisPool(config, host, port);
	}
	
	@Override
	public<T> T get(String name, Class<T> type) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis.exists(name))
				return JSON.parseObject(jedis.get(name), type);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return null;
	}
	
	@Override
	public void set(String name, Object value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(name, JSON.toJSONString(value));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}
	
}
