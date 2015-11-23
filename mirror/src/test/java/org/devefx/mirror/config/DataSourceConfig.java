package org.devefx.mirror.config;

import org.devefx.mirror.beans.factory.FactoryBean;
import org.devefx.mirror.beans.factory.InitializingBean;

import com.alibaba.druid.pool.DruidDataSource;

public class DataSourceConfig implements FactoryBean<DruidDataSource>, InitializingBean {

	private DruidDataSource dataSource;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		dataSource.setUrl("");
		dataSource.setUsername("");
		dataSource.setPassword("");
	}

	@Override
	public DruidDataSource getObject() throws Exception {
		return dataSource;
	}
	@Override
	public Class<?> getObjectType() {
		return DruidDataSource.class;
	}
	@Override
	public boolean isSingleton() {
		return false;
	}
}
