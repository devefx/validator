package org.devefx.mirror.core.struct;

public interface Property {
	String getName();
	Class<?> getType();
	boolean isCollection();
}
