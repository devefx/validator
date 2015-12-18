package org.devefx.validator.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonResult
 * @author： youqian.yue
 * @date： 2015-12-18 上午10:17:21
 */
public class JsonResult implements Map<String, Object> {
	private Map<String, Object> map;
	public JsonResult() {
		map = new HashMap<String, Object>();
	}
	@Override
	public int size() {
		return map.size();
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	@Override
	public Object get(Object key) {
		return map.get(key);
	}
	@Override
	public Object put(String key, Object value) {
		return map.put(key, value);
	}
	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}
	@Override
	public void clear() {
		map.clear();
	}
	@Override
	public Set<String> keySet() {
		return map.keySet();
	}
	@Override
	public Collection<Object> values() {
		return map.values();
	}
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
	@Override
	public String toString() {
		StringBuffer sbuff = new StringBuffer();
		handler(sbuff, map);
		return sbuff.toString();
	}
	private void handler(StringBuffer sbuff, Object value) {
		Class<?> valueClass = value.getClass();
		if (valueClass.isArray()) {
			sbuff.append("[");
			Object[] array = (Object[]) value;
			for (int i = 0, n = array.length; i < n; i++) {
				if (i != 0) {
					sbuff.append(", ");
				}
				handler(sbuff, array[i]);
			}
			sbuff.append("]");
		} else if (Iterable.class.isAssignableFrom(valueClass)) {
			sbuff.append("[");
			Iterator<Object> it = ((Iterable<Object>) value).iterator();
			for (int i = 0; it.hasNext(); i++) {
				if (i != 0) {
					sbuff.append(", ");
				}
				handler(sbuff, it.next());
			}
			sbuff.append("]");
		} else if (Map.class.isAssignableFrom(valueClass)) {
			sbuff.append("{");
			String token = "";
			Map<Object, Object> map = (Map<Object, Object>) value;
			for (Entry<Object, Object> entry : map.entrySet()) {
				sbuff.append(token);
				sbuff.append("\"");
				sbuff.append(entry.getKey());
				sbuff.append("\":");
				handler(sbuff, entry.getValue());
				token = ", ";
			}
			sbuff.append("}");
		} else if (Number.class.isAssignableFrom(valueClass)
				|| valueClass.isPrimitive() || value instanceof Boolean) {
			sbuff.append(value);
		} else if (value instanceof String) {
			sbuff.append("\"");
			sbuff.append(value);
			sbuff.append("\"");
		} else {
			sbuff.append("{");
			String token = "";
			
			Set<String> exclude = new HashSet<String>();
			Pattern pattern = Pattern.compile("get([A-Z].*)");
			Method[] methods = valueClass.getDeclaredMethods();
			for (Method method : methods) {
				Matcher matcher = pattern.matcher(method.getName());
				if (matcher.find()) {
					String match = matcher.group(1);
					String keyName = match.substring(0, 1).toLowerCase()
							+ match.substring(1);
					exclude.add(keyName);
					sbuff.append(token);
					sbuff.append("\"");
					sbuff.append(keyName);
					sbuff.append("\":");
					try {
						handler(sbuff, method.invoke(value));
					} catch (Exception e) {
					}
					token = ", ";
				}
			}
			Field[] fields = valueClass.getDeclaredFields();
			for (Field field : fields) {
				String keyName = field.getName();
				if (!exclude.contains(keyName)) {
					sbuff.append(token);
					sbuff.append("\"");
					sbuff.append(keyName);
					sbuff.append("\":");
					try {
						handler(sbuff, field.get(value));
					} catch (IllegalAccessException e) {
					}
					token = ", ";
				}
			}
			
			sbuff.append("}");
		}
	}
}
