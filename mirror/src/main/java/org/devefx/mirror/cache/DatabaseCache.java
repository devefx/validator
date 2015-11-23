package org.devefx.mirror.cache;

public interface DatabaseCache {
	public<T> T get(String name, Class<T> type);
	
	public void set(String name, Object value);
}
