package org.devefx.mirror.sqlmap.engine.builder.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Entity;
import org.devefx.mirror.annotation.Table;
import org.devefx.mirror.beans.util.ClassUtils;
import org.devefx.mirror.core.struct.Model;
import org.devefx.mirror.core.struct.impl.EntityProperty;
import org.devefx.mirror.core.struct.impl.PrimitiveProperty;
import org.devefx.mirror.sqlmap.client.SqlMapClient;
import org.devefx.mirror.utils.ReflectionUtils;
import org.devefx.mirror.utils.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;

public class ConfigParser {
	private EntityResolver entityResolver;
	private Properties properties;
	private Map<Class<?>, Model> classModel;
	private Map<String, Model> tableModel;
	private Map<String, Object> beanIdMap;
	private Map<Class<?>, List<Object>> beanClassMap;
	private Set<Object> beans;
	
	public ConfigParser() {
		properties = new Properties();
		classModel = new HashMap<Class<?>, Model>();
		tableModel = new HashMap<String, Model>();
		beanIdMap = new HashMap<String, Object>();
		beanClassMap = new HashMap<Class<?>, List<Object>>();
		beans = new HashSet<Object>();
		entityResolver = new SqlMapClasspathEntityResolver();
	}
	
	public SqlMapClient parse(Reader reader) {
		try {
			SAXReader saxReader = new SAXReader();
			saxReader.setEntityResolver(entityResolver);
			return parse(saxReader.read(reader));
		} catch (Exception e) {
			throw new RuntimeException("Error occurred.  Cause: " + e, e);
		}
	}
	public SqlMapClient parse(InputStream inputStream) {
		return parse(new InputStreamReader(inputStream));
	}
	/**
	 * parse config file
	 * @param document
	 * @return SqlMapClient
	 */
	protected SqlMapClient parse(Document document) {
		try {
			initProperty(document);
			initBean(document);
			scanModel(document);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SqlMapClient client = new SqlMapClient(this);
		return client;
	}
	/**
	 * load property file
	 * @param document void
	 */
	protected void initProperty(Document document) throws FileNotFoundException {
		ClassLoader loader = getClass().getClassLoader();
		List<Node> list = document.selectNodes("/configuration/property-file/@location");
		for (Node node : list) {
			Properties prop = new Properties();
			try {
				InputStream is = loader.getResourceAsStream(node.getText()); 
				if (is == null) 
					throw new FileNotFoundException("配置文件未找到");
				prop.load(loader.getResourceAsStream(node.getText()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			properties.putAll(prop);
		}
	}
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	/**
	 * scan model
	 * @param document void
	 * @throws Exception 
	 */
	protected void scanModel(Document document) throws Exception {
		List<Node> list = document.selectNodes("/configuration/model-scan/@base-package");
		for (Node node : list) {
			String basePackage = node.getText();
			if (StringUtils.isBlank(basePackage))
				continue;
			final Set<EntityProperty> entityTask = new HashSet<EntityProperty>();
			Set<Class<?>> classes = ClassUtils.getClasses(basePackage);
			for (Class<?> clazz : classes) {
				// is Model
				if (clazz.isAnnotationPresent(Table.class)) {
					Table table = clazz.getAnnotation(Table.class);
					Model model = new Model(clazz, table);
					// find Column & Entity
					for (Field field : clazz.getDeclaredFields()) {
						if (field.isAnnotationPresent(Column.class)) {
							Column column = field.getAnnotation(Column.class);
							String columnName = column.value();
							if (StringUtils.isBlank(columnName))
								columnName = field.getName();
							model.addMapping(columnName, new PrimitiveProperty(field));
						} else if (field.isAnnotationPresent(Entity.class)) {
							Entity entity = field.getAnnotation(Entity.class);
							EntityProperty property = new EntityProperty(field, entity.lazy());
							model.addMapping(entity.value(), property);
							entityTask.add(property);
						}
					}
					tableModel.put(model.getTableName(), model);
					classModel.put(model.getModelClass(), model);
				}
			}
			for (EntityProperty property : entityTask) {
				Model model = classModel.get(property.getType());
				if (model == null)
					throw new Exception("未找到Model");
				property.setModel(model);
			}
		}
	}
	public Model getModel(Class<?> clazz) {
		return classModel.get(clazz);
	}
	public Model getModel(String tableName) {
		return tableModel.get(tableName);
	}
	/**
	 * init bean
	 * @param document void
	 * @throws Exception 
	 */
	protected void initBean(Document document) throws Exception {
		List<Node> list = document.selectNodes("/configuration/bean");
		for (Node node : list) {
			String id = node.valueOf("@id");
			String className = node.valueOf("@class");
			String initMethoad = node.valueOf("@init-method");
			if (className == null)
				throw new Exception("class不能为空");
			// 加载并创建对象
			Class<?> clazz = Class.forName(className);
			Object bean = clazz.newInstance();
			// 初始化对象属性
			List<Node> propertys = node.selectNodes("property");
			for (Node property : propertys) {
				String name = property.valueOf("@name");
				String value = property.valueOf("@value");
				String ref = property.valueOf("@ref");
				if (StringUtils.isNotBlank(ref)) {
					// ref object
				} else {
					if (StringUtils.isBlank(value)) {
						value = property.getText();
					}
					ReflectionUtils.setValue(bean, name, parseExpr(value));
				}
			}
			// call object init method
			if (StringUtils.isNotBlank(initMethoad))
				ReflectionUtils.call(bean, initMethoad);
			registry(id, bean);
		}
	}
	protected String parseExpr(String expression) throws Exception {
		StringBuffer result = new StringBuffer(expression.length());
		Pattern pattern = Pattern.compile("\\$\\{(.+)\\}");
		Matcher matcher = pattern.matcher(expression);
		while (matcher.find()) {
			String replacement = getProperty(matcher.group(1), null);
			if (replacement == null)
				throw new Exception("");
			matcher.appendReplacement(result, replacement);
		}
		matcher.appendTail(result);
		return result.toString();
	}
	protected void registry(String beanId, Object bean) {
		if (StringUtils.isNotBlank(beanId))
			beanIdMap.put(beanId, bean);
		Class<?> beanClass = bean.getClass();
		List<Object> list = beanClassMap.get(beanClass);
		if (list == null) {
			list = new ArrayList<Object>();
			beanClassMap.put(beanClass, list);
		}
		list.add(bean);
		beans.add(bean);
	}
	public<T> T getBean(String beanId) {
		Object bean = beanIdMap.get(beanId);
		return bean != null ? (T) bean : null;
	}
	public<T> T getBean(Class<?> beanClass) throws Exception {
		List<Object> list = beanClassMap.get(beanClass);
		if (list != null) {
			int n = list.size();
			if (n > 1)
				throw new Exception("");
			return n != 0 ? (T) list.get(0) : null;
		}
		return null;
	}
	public Iterator<Object> getAllBean() {
		return beans.iterator();
	}
}
