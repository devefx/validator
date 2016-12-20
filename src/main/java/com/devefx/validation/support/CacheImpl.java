package com.devefx.validation.support;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devefx.validation.Cache;
import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Valid;

/**
 * CacheImpl
 * Created by YYQ on 2016/9/19.
 */
public class CacheImpl implements Cache {

	static final Logger log = LoggerFactory.getLogger(CacheImpl.class);
	
	Map<AnnotatedElement, List<Validator>> validCache = 
			new ConcurrentHashMap<AnnotatedElement, List<Validator>>(5);
	
	@Override
	public List<Validator> get(AnnotatedElement key) {
		List<Validator> value = validCache.get(key);
		if (value == null && key.isAnnotationPresent(Valid.class)) {
			value = new ArrayList<Validator>();
			Valid valid = key.getAnnotation(Valid.class);
			for (Class<? extends Validator> validClass : valid.value()) {
				try {
					Validator validator = acquireInstance(validClass);
					validator.setup();
					value.add(validator);
				} catch (Exception e) {
					log.error("An error occurred：", e);
					e.printStackTrace();
				}
			}
			put(key, value);
		}
		return value;
	}
	
	@Override
	public Validator acquireInstance(Class<? extends Validator> validClass) throws Exception {
	    return validClass.newInstance();
	}

	@Override
	public void put(AnnotatedElement key, List<Validator> value) {
		validCache.put(key, value);
	}

	@Override
	public void evict(AnnotatedElement key) {
		validCache.remove(key);
	}

	@Override
	public void clear() {
		validCache.clear();
	}
}
