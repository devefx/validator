package org.devefx.mirror.beans;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.devefx.mirror.utils.StringUtils;

/**
 * BeanManager
 * @author： youqian.yue
 * @date： 2015-11-10 上午11:00:49
 */
public final class BeanManager {
	
	private final Map<String, Object> beansId;
	private final Map<Type, List<Object>> beansType;
	private final List<Object> beans;
	
	private BeanManager() {
		this.beansId = new HashMap<String, Object>();
		this.beansType = new HashMap<Type, List<Object>>();
		this.beans = new ArrayList<Object>();
	}
	
	private static BeanManager beanManager;
	public static BeanManager getInstance() {
		if (beanManager == null) {
			beanManager = new BeanManager();
		}
		return beanManager;
	}
	
	public void registry(String beanId, Object object) throws Exception {
		if (StringUtils.isNotBlank(beanId)) {
			if (beansId.containsKey(beanId)) {
				throw new Exception(beanId + " 此ID已经被注册");
			}
			beansId.put(beanId, object);
		}
		Type beanType = object.getClass();
		List<Object> list = beansType.get(beanType);
		if (list == null) {
			list = new ArrayList<Object>();
			beansType.put(beanType, list);
		}
		list.add(object);
		
		beans.add(object);
	}
	
	public<T> T getBean(String beanId) {
		Object bean = beansId.get(beanId);
		return bean != null ? (T) bean : null;
	}
	
	public<T> T getBean(Type beanType) throws Exception {
		List<Object> list = beansType.get(beanType);
		if (list.size() > 1) {
			throw new Exception("");
		} else if (list.size() == 1) {
			Object bean = list.get(0);
			return bean != null ? (T) bean : null;
		}
		return null;
	}
	
	public Iterator<Object> getBeans() {
		return beans.iterator();
	}
}
