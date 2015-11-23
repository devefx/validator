package org.devefx.discard;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.devefx.mirror.beans.BeanManager;
import org.devefx.mirror.beans.annotation.Component;
import org.devefx.mirror.beans.factory.FactoryBean;
import org.devefx.mirror.beans.factory.TaskBean;
import org.devefx.mirror.beans.factory.InitializingBean;
import org.devefx.mirror.beans.util.ClassUtils;
import org.devefx.mirror.cache.DatabaseCache;
import org.devefx.mirror.cache.impl.DatabaseCacheImpl;
import org.devefx.mirror.config.DruidPreparedStatement;
import org.devefx.mirror.utils.ReflectionUtils;
import org.devefx.mirror.utils.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class ConfigParser {
	private static final ClassLoader loader = ConfigParser.class.getClassLoader();
	
	private BeanManager beanManager = BeanManager.getInstance();
	
	private SqlClient sqlClient;
	
	private Properties properties;
	
	private List<TaskBean> tasks = new ArrayList<TaskBean>();
	
	public ConfigParser(String configLocation) throws Exception {
		if (StringUtils.isBlank(configLocation)) 
			throw new Exception("配置文件路径不能为空");
		if (configLocation.startsWith("classpath:")) 
			configLocation = configLocation.substring(10);
		
		InputStream is = loader.getResourceAsStream(configLocation);
		if (is == null) 
			throw new Exception("配置文件未找到");
		
		init(is);
		
		DataSource dataSource = beanManager.getBean("dataSource");
		DatabaseCache cache = new DatabaseCacheImpl();//beanManager.getBean("jedisCache");
		SqlClientImpl sqlClientImpl = new SqlClientImpl(dataSource, cache);
		sqlClientImpl.setProxyStmtClass(DruidPreparedStatement.class);
		
		this.sqlClient = sqlClientImpl;
	}
	
	public SqlClient getSqlClient() {
		return sqlClient;
	}
	
	private boolean init(InputStream is) throws Exception {
		Document document = null;
		SAXReader reader = new SAXReader(false);
		try {
			document = reader.read(is);
		} catch (Exception e) {
		}
		
		initProperty(document);
		initBean(document);
		
		
		initBean();
		return true;
	}
	
	
	/**
	 * 加载配置文件
	 * @author：youqian.yue
	 * @date： 2015-11-2 下午5:54:20
	 * @param document void
	 * @throws Exception 
	 */
	private void initProperty(Document document) throws Exception {
		properties = new Properties();
		List<Node> list = document.selectNodes("/configuration/property-file/@location");
		for (Node node : list) {
			Properties prop = new Properties();
			try {
				InputStream is = loader.getResourceAsStream(node.getText()); 
				if (is == null) 
					throw new Exception("配置文件未找到");
				prop.load(loader.getResourceAsStream(node.getText()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			properties.putAll(prop);
		}
	}
	
	/**
	 * 创建bean
	 * @author：youqian.yue
	 * @date： 2015-11-2 下午6:00:05
	 * @param document void
	 * @throws Exception 
	 */
	private void initBean(Document document) throws Exception {
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
				boolean notRef = StringUtils.isBlank(ref);
				if (notRef && StringUtils.isBlank(value)) {
					value = property.getText();
				}
				if (notRef) {
					ReflectionUtils.setValue(bean, name, parse(value));
				} else {
					Object obj = beanManager.getBean(name);
					if (obj instanceof FactoryBean<?>)
						obj = ((FactoryBean<?>) obj).getObject();
					if (obj != null) {
						ReflectionUtils.setValue(bean, name, obj);
					} else {
						tasks.add(new TaskBean(bean, name, ref));
					}
				}
			}
			if (StringUtils.isNotBlank(initMethoad)) {
				ReflectionUtils.call(bean, initMethoad);
			}
			beanManager.registry(id, bean);
		}
		componentScan(document);
		doTask();
	}
	/**
	 * 扫描组件
	 * @author：youqian.yue
	 * @date： 2015-11-10 下午6:01:17
	 * @param document void
	 * @throws Exception 
	 */
	private void componentScan(Document document) throws Exception {
		List<Node> list = document.selectNodes("/configuration/component-scan");
		for (Node node : list) {
			String basePackage = node.valueOf("@base-package");
			Set<Class<?>> classes = ClassUtils.getClasses(basePackage);
			for (Class<?> clazz : classes) {
				if (clazz.isAnnotationPresent(Component.class)) {
					Component classComponent = clazz.getAnnotation(Component.class);
					
					Object bean = clazz.newInstance();
					for (Field field : clazz.getDeclaredFields()) {
						if (field.isAnnotationPresent(Component.class)) {
							Component fieldComponent = field.getAnnotation(Component.class);
							String beanId = fieldComponent.value();
							
							Object obj = null;
							if (StringUtils.isNotBlank(beanId)) {
								obj = beanManager.getBean(beanId);
							} else {
								obj = beanManager.getBean(field.getType());
							}
							if (obj instanceof FactoryBean<?>)
								obj = ((FactoryBean<?>) obj).getObject();
							if (obj != null) {
								field.setAccessible(true);
								field.set(bean, obj);
							} else {
								tasks.add(new TaskBean(bean, field.getName(), beanId, field.getType()));
							}
						}
					}
					beanManager.registry(classComponent.value(), bean);
				}
				
			}
		}
	}
	/**
	 * 处理任务
	 * @author：youqian.yue
	 * @date： 2015-11-3 上午10:57:13
	 * @throws Exception void
	 */
	private void doTask() throws Exception {
		for (TaskBean beanTask : tasks) {
			beanTask.execute(beanManager);
		}
		tasks.clear();
	}
	/**
	 * 初始化bean
	 * @author：youqian.yue
	 * @date： 2015-11-3 上午11:09:17
	 * @throws Exception void
	 */
	private void initBean() throws Exception {
		Iterator<Object> iterator = beanManager.getBeans();
		while (iterator.hasNext()) {
			Object bean = iterator.next();
			if (bean instanceof InitializingBean)
				((InitializingBean) bean).afterPropertiesSet();
		}
	}
	
	/**
	 * 解析参数
	 * @author：youqian.yue
	 * @date： 2015-11-3 上午10:57:22
	 * @param text
	 * @return String
	 */
	private String parse(String text) {
		if (!StringUtils.isBlank(text)) {
			StringBuffer buffer = new StringBuffer(text.length());
			Pattern pattern = Pattern.compile("\\$\\{([a-z0-9._]+)\\}", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String key = matcher.group(1);
				String value = properties.getProperty(key);
				matcher.appendReplacement(buffer, value == null ? "":value);
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		}
		return text;
	}
	
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
