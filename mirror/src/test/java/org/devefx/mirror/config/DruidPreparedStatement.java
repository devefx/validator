package org.devefx.mirror.config;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;

import org.devefx.mirror.core.ProxyPreparedStatement;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.mysql.jdbc.JDBC4PreparedStatement;

public class DruidPreparedStatement implements ProxyPreparedStatement {
	
	private DruidPooledPreparedStatement stmt;
	
	@Override
	public PreparedStatement setProxy(PreparedStatement stmt) {
		this.stmt = (DruidPooledPreparedStatement) stmt;
		return stmt;
	}
	
	@Override
	public String getStaticSql() {
		Class<?> clazz = stmt.getClass();
		try {
			Field field = clazz.getDeclaredField("stmt");
			field.setAccessible(true);
			JDBC4PreparedStatement statement = (JDBC4PreparedStatement) field.get(stmt);
			return statement.asSql();
		} catch (Exception e) {
		}
		return null;
	}
}
