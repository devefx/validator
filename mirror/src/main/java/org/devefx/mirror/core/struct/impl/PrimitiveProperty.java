package org.devefx.mirror.core.struct.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.devefx.mirror.core.struct.Property;

public class PrimitiveProperty implements Property {
	private String name;
	private Class<?> type;
	private boolean isCollection;
	public PrimitiveProperty(Field field) {
		this.name = field.getName();
		this.type = field.getType();
		if (Collection.class.isAssignableFrom(type)) {
			Type type = field.getGenericType();
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				this.type = (Class<?>) pt.getActualTypeArguments()[0];
			}
			isCollection = true;
		}
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Class<?> getType() {
		return type;
	}
	@Override
	public boolean isCollection() {
		return isCollection;
	}
}
