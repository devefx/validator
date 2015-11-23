package org.devefx.mirror.sqlmap.client;

import java.util.HashMap;
import java.util.Map;

public class SqlMapType {
	protected static final Map<Class<?>, String> typeMap = new HashMap<Class<?>, String>();
	static {
		setMap("Boolean", boolean.class, Boolean.class);
		setMap("Byte", byte.class, Byte.class);
		setMap("Short", short.class, Short.class);
		setMap("Int", int.class, Integer.class);
		setMap("Long", long.class, Long.class);
		setMap("Float", float.class, Float.class);
		setMap("Double", double.class, Double.class);
		setMap("Bytes", byte[].class, Byte[].class);
		
		setMap("Date", java.sql.Date.class);
		setMap("Time", java.sql.Time.class);
		setMap("Timestamp", java.sql.Timestamp.class, java.util.Date.class);
		
		setMap("BigDecimal", java.math.BigDecimal.class);
		setMap("Blob", java.sql.Blob.class);
		setMap("Clob", java.sql.Clob.class);
		setMap("String", String.class);
		setMap("URL", java.net.URL.class);
	}
	private static void setMap(String type, Class<?> ...classes) {
		for (Class<?> clazz : classes) {
			typeMap.put(clazz, type);
		}
	}
}
