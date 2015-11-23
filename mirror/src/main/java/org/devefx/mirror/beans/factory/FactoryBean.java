package org.devefx.mirror.beans.factory;

public interface FactoryBean<T> {
	public T getObject() throws Exception;
	
	public Class<?> getObjectType();
	
	public boolean isSingleton();
}
