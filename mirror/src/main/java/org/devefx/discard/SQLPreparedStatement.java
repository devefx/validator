package org.devefx.discard;

import java.sql.SQLException;

public interface SQLPreparedStatement {
	/**
	 * 处理动态SQL
	 * @param sql
	 * @throws SQLException void
	 */
	void parse(String sql) throws SQLException;
	
	void setObject(int parameterIndex, Object x) throws SQLException;
	
	void setString(int parameterIndex, String x) throws SQLException;
	
	void setInteger(int parameterIndex, Integer x) throws SQLException;
	/**
	 * 获取静态SQL
	 * @return String
	 */
	String getStaticSql();
}
