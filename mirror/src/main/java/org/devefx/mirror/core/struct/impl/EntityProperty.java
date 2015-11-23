package org.devefx.mirror.core.struct.impl;

import java.lang.reflect.Field;

import org.devefx.mirror.core.struct.Model;

public class EntityProperty extends PrimitiveProperty {
	private Model model;
	private boolean lazy;
	public EntityProperty(Field field, boolean lazy) {
		super(field);
		this.lazy = lazy;
	}
	public void setModel(Model model) {
		this.model = model;
	}
	public Model getModel() {
		return model;
	}
	public boolean isLazy() {
		return lazy;
	}
}
