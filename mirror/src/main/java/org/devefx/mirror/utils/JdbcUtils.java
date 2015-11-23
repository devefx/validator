package org.devefx.mirror.utils;

public class JdbcUtils implements JdbcConstants {
	public static String getPreparedStatementClassName(String url) {
		if (url.startsWith("jdbc:mysql:")) {
			return MYSQL_PREPARED_STATEMENT;
		} else if (url.startsWith("jdbc:oracle:")) {
			return ORACLE_PREPARED_STATEMENT;
		} else if (url.startsWith("jdbc:sqlserver:")) {
			return MSSQL_PREPARED_STATEMENT;
		}
		return null;
	}
}
