package org.devefx.mirror.sqlmap.client;

import java.sql.SQLException;
import java.util.List;

/**
 * SqlMapExecutor
 * @author： youqian.yue
 * @date： 2015-11-20 下午3:28:18
 */
public interface SqlMapExecutor {
	<T> T query(Class<T> type, Object key) throws SQLException;
	boolean update(Object model) throws SQLException;
	boolean delete(Object model) throws SQLException;
	boolean insert(Object model) throws SQLException;
	/**
	 * 执行SQL查询一条数据
	 * @param sql 要查询的预处理sql语句
	 * @param type 需要返回的类型
	 * @param parameters 预处理参数数组
	 * @return T
	 * @throws SQLException
	 */
	<T> T query(String sql, Class<T> type, Object... parameters) throws SQLException;
	/**
	 * 执行SQL查询多条数据
	 * @param sql 要查询的预处理sql语句
	 * @param type 需要返回的类型
	 * @param parameters 预处理参数数组
	 * @return List<T>
	 * @throws SQLException
	 */
	<T> List<T> queryList(String sql, Class<T> type, Object... parameters) throws SQLException;
	/**
	 * 执行SQL更新语句
	 * @param sql 要更新的预处理sql语句
	 * @param parameters 预处理参数数组
	 * @return int 受影响的行数
	 * @throws SQLException
	 */
	int execute(String sql, Object... parameters) throws SQLException;
	/**
	 * 批处理执行SQL更新语句
	 * @param sql 要更新的预处理sql语句
	 * @param parameters 预处理参数数组
	 * @return int[] 受影响的行数
	 * @throws SQLException
	 */
	int[] executeBatch(String sql, Object[]... parameters) throws SQLException;
}
