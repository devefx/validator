package org.devefx.mirror.beans.factory;

import java.lang.reflect.Type;

import org.devefx.mirror.beans.BeanManager;
import org.devefx.mirror.utils.ReflectionUtils;
import org.devefx.mirror.utils.StringUtils;

public class TaskBean {
	private Object bean;
	private String property;
	private String beanId;
	private Type beanType;
	public TaskBean(Object bean, String property, String beanId) {
		this(bean, property, beanId, null);
	}
	public TaskBean(Object bean, String property, Type beanType) {
		this(bean, property, null, beanType);
	}
	public TaskBean(Object bean, String property, String beanId, Type beanType) {
		this.bean = bean;
		this.property = property;
		this.beanId = beanId;
		this.beanType = beanType;
	}
	public void execute(BeanManager beanManager) throws Exception {
		Object value = null;
		if (StringUtils.isNotBlank(beanId)) {
			value = beanManager.getBean(beanId);
		} else {
			value = beanManager.getBean(beanType);
		}
		if (value instanceof FactoryBean<?>)
			value = ((FactoryBean<?>) value).getObject();
		if (value != null) {
			ReflectionUtils.setValue(bean, property, value);
		}
	}
}