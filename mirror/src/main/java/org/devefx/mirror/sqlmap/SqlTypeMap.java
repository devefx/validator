package org.devefx.mirror.sqlmap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.utils.StringUtils;

public class SqlTypeMap {
	
	private static final Map<Class<?>, String> typeMap = new HashMap<Class<?>, String>();
	
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
	
	public static Object convert(Object value, Class<?> type) {
		String name = typeMap.get(type);
		if (value != null && name != null) {
			String s = value.toString();
			if (name.equals("Boolean"))
				return Boolean.parseBoolean(s);
			if (name.equals("Byte"))
				return Byte.parseByte(s);
			if (name.equals("Short"))
				return Short.parseShort(s);
			if (name.equals("Int"))
				return Integer.parseInt(s);
			if (name.equals("Long"))
				return Long.parseLong(s);
			if (name.equals("Float"))
				return Float.parseFloat(s);
			if (name.equals("Double"))
				return Double.parseDouble(s);
			if (name.equals("BigDecimal"))
				return new BigDecimal(s);
			if (name.equals("String"))
				return s;
			if (name.equals("URL"))
				try {
					return new URL(s);
				} catch (MalformedURLException e) { }
		}
		return value;
	}
	
	public static<T> T extractData(ResultSet rs, Class<T> requiredType) throws SQLException {
		String type = typeMap.get(requiredType);
		if (type != null && rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rsmd.getColumnCount() != 1)
				throw new RuntimeException("结果集列数大于1.");
			Class<ResultSet> clazz = ResultSet.class;
			try {
				Method method = clazz.getMethod("get" + type, int.class);
				return (T) method.invoke(rs, 1);
			} catch (Exception e) { }
		} else if (Map.class.isAssignableFrom(requiredType) && rs.next()) {
			return (T) SqlTypeMap.mapRow(rs);
		} else if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			Map<String, Integer> columnMap = new HashMap<String, Integer>();
			for (int i = 1, n = rsmd.getColumnCount() + 1; i < n; i++) {
				String key = rsmd.getColumnName(i);
				columnMap.put(key, i);
			}
			try {
				T object = requiredType.newInstance();
				Field[] fields = requiredType.getDeclaredFields();
				for (Field field : fields) {
					if (field.isAnnotationPresent(Column.class)) {
						Column column = field.getAnnotation(Column.class);
						String fieldName = field.getName();
						if (!StringUtils.isBlank(column.value()))
							fieldName = column.value();
						Integer index = columnMap.get(fieldName);
						if (index != null) {
							String methodName = "set" + StringUtils.firstToUpperCase(fieldName);
							Object value = getColumnValue(rs, index, field.getType());
							try {
								Method method = requiredType.getMethod(methodName, field.getType());
								method.invoke(object, value);
							} catch (NoSuchMethodException e) {
								field.setAccessible(true);
								field.set(object, value);
							}
						}
					}
				}
				return object;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static<T> T getColumnValue(ResultSet rs, int index, Class<T> requiredType) {
		String type = typeMap.get(requiredType);
		if (type != null) {
			Class<ResultSet> clazz = ResultSet.class;
			try {
				Method method = clazz.getMethod("get" + type, int.class);
				return (T) method.invoke(rs, index);
			} catch (Exception e) { }
		}
		return null;
	}
	
	private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> result = new HashMap<String, Object>();
		for (int i = 1; i <= columnCount; i++) {
			String key = rsmd.getColumnName(i);
			Object value = rs.getObject(i);
			result.put(key, value);
		}
		return result;
	}
}
