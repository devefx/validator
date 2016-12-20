package com.devefx.validation;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Cache
 * Created by YYQ on 2016/9/19.
 */
public interface Cache {
	
	public List<Validator> get(AnnotatedElement key);

	public Validator acquireInstance(Class<? extends Validator> validClass) throws Exception;
	
	public void put(AnnotatedElement key, List<Validator> value);
	
	public void evict(AnnotatedElement key);
	
	public void clear();
}
