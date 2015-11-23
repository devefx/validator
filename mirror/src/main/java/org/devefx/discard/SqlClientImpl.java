package org.devefx.discard;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Table;
import org.devefx.mirror.cache.DatabaseCache;
import org.devefx.mirror.core.ProxyPreparedStatement;
import org.devefx.mirror.sqlmap.SqlTypeMap;
import org.devefx.mirror.utils.ReflectionUtils;
import org.devefx.mirror.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlClientImpl implements SqlClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlClientImpl.class);
	private static final String SQL_SELECT_BY_KEY = "SELECT * FROM %s WHERE %s = ?";
	private static final String SQL_DELETE_BY_KEY = "DELETE FROM %s WHERE %s = ?";
	private static final String CACHE_TOKEN = "#";
	
	private DataSource dataSource;
	private DatabaseCache cache;
	private Class<? extends ProxyPreparedStatement> proxyStmtClass;
	
	private DatabaseCacheHandle cacheHandle;
	
	public SqlClientImpl(DataSource dataSource) {
		this(dataSource, null);
	}
	public SqlClientImpl(DataSource dataSource, DatabaseCache cache) {
		this.dataSource = dataSource;
		this.cache = cache;
	}
	
	public void setProxyStmtClass(Class<? extends ProxyPreparedStatement> proxyStmtClass) {
		this.proxyStmtClass = proxyStmtClass;
	}
	
	@Override
	public <T> T query(String sql, Class<T> type, Object... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			ProxyPreparedStatement proxyStmt = null;
			try {
				proxyStmt = proxyStmtClass.newInstance();
				statement = proxyStmt.setProxy(conn.prepareStatement(sql));
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("create ProxyPreparedStatement failure");
			}
			for (int i = 0; i < parameters.length; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[sql]{}", sql);
				LOGGER.debug("[parameters]{}", parameters);
				if (proxyStmt != null) {
					LOGGER.debug("[staticSql]{}", proxyStmt.getStaticSql());
				}
			}
			rs = statement.executeQuery();
			return SqlTypeMap.extractData(rs, type);
		} finally {
			closeConnection(conn, rs);
		}
	}

	@Override
	public <T> List<T> queryList(String sql, Class<T> type, Object... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0; i < parameters.length; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[sql]{}", sql);
				LOGGER.debug("[parameters]{}", parameters);
			}
			rs = statement.executeQuery();
			List<T> list = new ArrayList<T>(rs.getRow());
			while (true) {
				T data = SqlTypeMap.extractData(rs, type);
				if (data == null) 
					break;
				list.add(data);
			}
			return list;
		} finally {
			closeConnection(conn, rs);
		}
	}

	@Override
	public int execute(String sql, Object... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0; i < parameters.length; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[sql]{}", sql);
				LOGGER.debug("[parameters]{}", parameters);
			}
			return statement.executeUpdate();
		} finally {
			closeConnection(conn, rs);
		}
	}

	@Override
	public int[] executeBatch(String sql, Object[]... parameters) throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement statement = conn.prepareStatement(sql);
			for (Object[] objects : parameters) {
				for (int i = 0; i < objects.length; i++) {
					statement.setObject(i + 1, objects[i]);
				}
				statement.addBatch();
			}
			int[] result = statement.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
			return result;
		} catch (SQLException e) {
			if (!conn.isClosed()) {
				conn.rollback();
			}
			e.printStackTrace();
		} finally {
			closeConnection(conn, rs);
		}
		return null;
	}

	@Override
	public <T> T load(Class<T> type, Object id) throws SQLException {
		Table table = checkModel(type);
		String tableName = table.value();
		String cacheKey = tableName + CACHE_TOKEN + id;
		if (cache != null) {
			T object = cache.get(cacheKey.toUpperCase(), type);
			if (object != null)
				return object;
		}
		T object = query(String.format(SQL_SELECT_BY_KEY, tableName, table.key()), type, id);
		if (object != null && cache != null) {
			cache.set(cacheKey.toUpperCase(), object);
		}
		return object;
	}

	@Override
	public boolean update(Object object) throws SQLException {
		Class<?> clazz = object.getClass();
		Table table = checkModel(clazz);
		
		Object key = null;
		boolean isChange = false;
		boolean rdKey = false;
		Object memory = cache.get(getModelKey(object), object.getClass());
		
		List<Object> parameters = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer("update ");
		sql.append(table.value());
		sql.append(" set ");
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				String columnName = field.getName();
				Object columnValue = ReflectionUtils.getValue(object, columnName);
				/** compare cache */
				if (memory != null) {
					Object memoryValue = ReflectionUtils.getValue(memory, columnName);
					if ((columnName == null && memoryValue == null) || (columnName != null && columnName.equals(memoryValue))
							|| (memoryValue != null && memoryValue.equals(columnValue))) {
						continue;
					}
					isChange = true;
				}
				Column column = field.getAnnotation(Column.class);
				if (StringUtils.isNotBlank(column.value()))
					columnName = column.value();
				boolean isKey = columnName.equals(table.key());
				if (isKey) {
					key = columnValue;
					rdKey = true;
				} else {
					parameters.add(columnValue);
					sql.append(columnName);
					sql.append(" = ?, ");
				}
			}
		}
		if (isChange) {
			if (!rdKey)
				key = ReflectionUtils.getValue(object, table.key());
			if (key == null)
				throw new SQLException("主键不能为空");
			parameters.add(key);
			int len = sql.length();
			sql.delete(len - 2, len);
			sql.append(" where ");
			sql.append(table.key());
			sql.append(" = ?");
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[sql]{}", sql);
				LOGGER.debug("[parameters]{}", parameters);
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Model No Change");
			}
		}
		return false;
	}

	@Override
	public boolean delete(Object object) throws SQLException {
		Class<?> clazz = object.getClass();
		Table table = checkModel(clazz);
		String keyColumn = table.key();
		String keyValue = ReflectionUtils.getValue(object, keyColumn);
		if (keyValue == null)
			throw new SQLException("主键不能为空");
		return execute(String.format(SQL_DELETE_BY_KEY, table.value(), keyColumn), keyValue) > 0;
	}

	@Override
	public boolean insert(Object object) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void closeConnection(Connection conn, ResultSet rs) {
		try {
			if (conn != null)
				conn.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
		}
	}
	
	private Table checkModel(Class<?> clazz) throws SQLException {
		if (!clazz.isAnnotationPresent(Table.class))
			throw new SQLException("不是数据模型");
		return clazz.getAnnotation(Table.class);
	}
	
	
	public static String getModelKey(Object model) {
		Class<?> clazz = null;
		if (model != null && (clazz = model.getClass()) != null && clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			Object id = ReflectionUtils.getValue(model, table.key());
			if (id != null)
				return new String(table.value() + CACHE_TOKEN + id).toUpperCase();
		}
		return null;
	}
	public static String getModelKey(Class<?> clazz, Object id) {
		if (id != null && clazz != null && clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			return new String(table.value() + CACHE_TOKEN + id).toUpperCase();
		}
		return null;
	}
}
