package org.devefx.mirror.cache.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.devefx.mirror.cache.DatabaseCache;

public class DatabaseCacheImpl implements DatabaseCache {

	private final Map<String, byte[]> cache = new HashMap<String, byte[]>();
	
	@Override
	public <T> T get(String name, Class<T> type) {
		return cache.containsKey(name) ? (T) deserialize(cache.get(name)) : null;
	}

	@Override
	public void set(String name, Object value) {
		cache.put(name, serialize(value));
	}
	
	private byte[] serialize(Object object) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
			objectOutputStream.writeObject(object);
			bytes = byteOutputStream.toByteArray();
			byteOutputStream.close();
			objectOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	private Object deserialize(byte[] bytes) {
		Object object = null;
		try {
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
			object = objectInputStream.readObject();
			byteInputStream.close();
			objectInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}
}
