package org.devefx.mirror.sqlmap.client;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.devefx.mirror.core.struct.Model;
import org.devefx.mirror.core.struct.Property;
import org.devefx.mirror.core.struct.impl.EntityProperty;
import org.devefx.mirror.core.struct.impl.PrimitiveProperty;
import org.devefx.mirror.sqlmap.engine.builder.xml.ConfigParser;
import org.devefx.mirror.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlMapClient extends SqlMapType implements SqlMapExecutor {
	private static final String SQL_DELETE_BY_KEY = "DELETE FROM %s WHERE %s = ?";
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlMapClient.class);
	private ConfigParser configParser;
	private DataSource dataSource;
	
	public SqlMapClient(ConfigParser configParser) {
		this.configParser = configParser;
		this.dataSource = this.configParser.getBean("dataSource");
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	@Override
	public <T> T query(Class<T> type, Object key) throws SQLException {
		Model model = configParser.getModel(type);
		if (model != null) {
			T result = query(model.getQuerySql(), type, key);
			if (result != null) {
				// TODO set cache
			}
			return result;
		}
		throw new SQLException("");
	}
	@Override
	public boolean update(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				List<Object> parameters = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer("UPDATE ");
				sql.append(model.getTableName());
				sql.append(" SET ");
				Object keyValue = null;
				boolean isChange = false;
				Map<String, Property> map = model.getColumnProperty();
				for (Map.Entry<String, Property> entry : map.entrySet()) {
					Property property = entry.getValue();
					if (property instanceof PrimitiveProperty) {
						String column = entry.getKey();
						Object value = ReflectionUtils.getValue(object, property.getName());
						// TODO compare cache
						if (column.equals(model.getPrimaryKey())) {
							keyValue = value;
						} else {
							parameters.add(value);
							if (isChange)
								sql.append(", ");
							sql.append(column);
							sql.append(" = ?");
						}
						isChange = true;
					}
				}
				if (isChange) {
					parameters.add(keyValue);
					sql.append(" WHERE ");
					sql.append(model.getPrimaryKey());
					sql.append(" = ?");
				}
				boolean result = execute(sql.toString(), parameters) > 0;
				if (result) {
					// TODO update cache
				}
				return result;
			}
			throw new SQLException("");
		}
		return false;
	}
	@Override
	public boolean delete(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				Object key = ReflectionUtils.getValue(object, model.getPrimaryKey());
				if (key == null)
					throw new SQLException("模型主键不能为空");
				boolean result = execute(String.format(SQL_DELETE_BY_KEY, model.getTableName(),
						model.getPrimaryKey()), key) > 0;
				if (result) {
					// TODO update cache
				}
				return result;
			}
			throw new SQLException("");
		}
		return false;
	}
	@Override
	public boolean insert(Object object) throws SQLException {
		if (object != null) {
			Class<?> modelClass = object.getClass();
			Model model = configParser.getModel(modelClass);
			if (model != null) {
				List<Object> parameters = new ArrayList<Object>();
				StringBuffer sql = new StringBuffer("INSERT INTO ");
				sql.append(model.getTableName());
				StringBuffer fieldSql = new StringBuffer();
				StringBuffer valueSql = new StringBuffer();
				boolean isFirst = true;
				Map<String, Property> map = model.getColumnProperty();
				for (Map.Entry<String, Property> entry : map.entrySet()) {
					Property property = entry.getValue();
					if (property instanceof PrimitiveProperty) {
						String column = entry.getKey();
						if (!column.equals(model.getPrimaryKey())) {
							Object value = ReflectionUtils.getValue(object, property.getName());
							if (!isFirst) {
								fieldSql.append(", ");
								valueSql.append(", ");
							}
							fieldSql.append(column);
							valueSql.append("?");
							parameters.add(value);
							isFirst = false;
						}
					}
				}
				sql.append("(");
				sql.append(fieldSql);
				sql.append(") VALUES(");
				sql.append(valueSql);
				sql.append(")");
				// output log
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("[sql]{}", sql);
					LOGGER.info("[parameters]{}", parameters);
				}
				boolean result = execute(sql.toString(), parameters) > 0;
				if (result) {
					// TODO update cache
				}
				return result;
			}
		}
		return false;
	}
	@Override
	public <T> T query(String sql, Class<T> type, Object... parameters)
			throws SQLException {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			for (int i = 0, n = parameters.length; i < n; i++) {
				statement.setObject(i + 1, parameters[i]);
			}
			rs = statement.executeQuery();
			return extractData(rs, type);
		} finally {
			closeConnection(conn, rs);
		}
	}
	@Override
	public <T> List<T> queryList(String sql, Class<T> type,
			Object... parameters) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int execute(String sql, Object... parameters) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int[] executeBatch(String sql, Object[]... parameters)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	private<T> T extractData(ResultSet rs, Class<T> requiredType) throws SQLException {
		String type = typeMap.get(requiredType);
		// base type
		if (type != null && rs.next()) {
			
		// map type
		} else if (Map.class.isAssignableFrom(requiredType) && rs.next()) {
			
		} else if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				T object = requiredType.newInstance();
				// is data model
				Model model = configParser.getModel(requiredType);
				if (model != null) {
					Map<String, Integer> columnMap = new HashMap<String, Integer>();
					for (int i = 1, n = rsmd.getColumnCount() + 1; i < n; i++) {
						String columnName = rsmd.getColumnName(i);
						String tableName = rsmd.getTableName(i);
						columnMap.put(tableName + "." + columnName, i);
					}
					List<String> closeList = new ArrayList<String>();
					extractData(object, model, rs, columnMap, closeList);
					return object;
				}
				// not data model
				return object;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return null;
	}
	private void extractData(Object object, Model model, ResultSet rs, Map<String, Integer> columnMap, List<String> closeList) {
		Map<String, Property> map = model.getColumnProperty();
		for (Map.Entry<String, Property> entry : map.entrySet()) {
			String column = entry.getKey();
			Property property = entry.getValue();
			if (property instanceof EntityProperty) {
				EntityProperty entityProperty = (EntityProperty) property;
				if (!entityProperty.isCollection()) {
					Model childModel = entityProperty.getModel();
					String addr = property.getClass() + "." + property.getName();
					if (childModel != null && !closeList.contains(addr)) {
						closeList.add(addr);
						try {
							Object childObject = childModel.getModelClass().newInstance();
							extractData(childObject, childModel, rs, columnMap, closeList);
							ReflectionUtils.setValue(object, property.getName(), childObject);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (property instanceof PrimitiveProperty) {
				Integer columnIndex = columnMap.get(model.getTableName() + "." + column);
				if (columnIndex != null) {
					Object value = getColumnValue(rs, columnIndex, property.getType());
					ReflectionUtils.setValue(object, property.getName(), value);
				}
			}
		}
	}
	private<T> T getColumnValue(ResultSet rs, int index, Class<T> requiredType) {
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
	private void closeConnection(Connection conn, ResultSet rs) {
		try {
			if (conn != null)
				conn.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			LOGGER.error("close connection fail");
		}
	}
}
