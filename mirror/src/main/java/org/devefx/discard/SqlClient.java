package org.devefx.discard;

import java.sql.SQLException;
import java.util.List;

/**
 * SqlClient
 * @author： youqian.yue
 * @date： 2015-11-17 上午10:50:41
 */
public interface SqlClient {
	public<T> T load(Class<T> type, Object id) throws SQLException;
	public boolean update(Object object) throws SQLException;
	public boolean delete(Object object) throws SQLException;
	public boolean insert(Object object) throws SQLException;
	/**
	 * 执行SQL查询一条数据
	 * @param sql 要查询的预处理sql语句
	 * @param type 需要返回的类型
	 * @param parameters 预处理参数数组
	 * @return T
	 * @throws SQLException
	 */
	public<T> T query(String sql, Class<T> type, Object ...parameters) throws SQLException;
	/**
	 * 执行SQL查询多条数据
	 * @param sql 要查询的预处理sql语句
	 * @param type 需要返回的类型
	 * @param parameters 预处理参数数组
	 * @return List<T>
	 * @throws SQLException
	 */
	public<T> List<T> queryList(String sql, Class<T> type, Object ...parameters) throws SQLException;
	/**
	 * 执行SQL更新语句
	 * @param sql 要更新的预处理sql语句
	 * @param parameters 预处理参数数组
	 * @return int 受影响的行数
	 * @throws SQLException
	 */
	public int execute(String sql, Object ...parameters) throws SQLException;
	/**
	 * 批处理执行SQL更新语句
	 * @param sql 要更新的预处理sql语句
	 * @param parameters 预处理参数数组
	 * @return int[] 受影响的行数
	 * @throws SQLException
	 */
	public int[] executeBatch(String sql, Object[] ...parameters) throws SQLException;
}
