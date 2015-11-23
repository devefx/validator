package org.devefx.mirror.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.devefx.discard.ConfigParser;
import org.devefx.discard.SqlClient;
import org.devefx.mirror.annotation.Table;
import org.devefx.mirror.cache.DatabaseCache;
import org.devefx.mirror.cache.impl.DatabaseCacheImpl;
import org.devefx.mirror.model.Equity;
import org.devefx.mirror.model.Member;
import org.devefx.mirror.utils.ReflectionUtils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSON;

public class Test1 {
	private static final String TOKEN = "#";
	
	private static final Map<String, Class<?>> TABLE_MODEL_MAP = new HashMap<String, Class<?>>();
	
	private static final DatabaseCache DATABASE_CACHE = new DatabaseCacheImpl();
	
	private static SqlClient sqlClient;
	
	public static void main(String[] args) throws Exception {
		init();
		
		ConfigParser configParser = new ConfigParser("classpath:Configuration.xml");
		
		sqlClient = configParser.getSqlClient();
		
		sqlClient.query("select * from t06_member_info where nickname = '?'", Member.class);
		
		
		Member member = sqlClient.load(Member.class, 159);//sqlClient.query("select * from t06_member_info where id = ?", Member.class, 1);
		System.out.println(JSON.toJSONString(member));
		
		
		member.setEmail("yyq8092@163.com");
		sqlClient.update(member);
		
		
		
		
		//String sql = "update t06_member_info set email='yyq8092@163.com', id=159, register_tm = now() where id=139";
		
		String sql = "update t06_member_info set email='yyq8092@163.com', id=159, register_tm = now() where id < 50";
		
		sql = "UPDATE t06_member_info,t06_equity SET t06_equity.member_id=t06_member_info.id WHERE t06_member_info.id=member_id";
		
		SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcUtils.MYSQL);
		SQLUpdateStatement statement = parser.parseUpdateStatement();
		
		List<SQLExprTableSource> tables = getTables(statement.getTableSource());
		List<SQLUpdateSetItem> setItems = statement.getItems();
		SQLBinaryOpExpr where = (SQLBinaryOpExpr) statement.getWhere();
		
		updateCache(tables, setItems, where);

	}
	
	public static void updateCache(List<SQLExprTableSource> tables, List<SQLUpdateSetItem> items, SQLBinaryOpExpr where) throws SQLException {
		/**
		 * where TABLE.PRIMARY_KEY = ?
		 */
		SQLExpr rightValue = where.getRight();
		if (where.getOperator() == SQLBinaryOperator.Equality && !(rightValue instanceof SQLPropertyExpr || rightValue instanceof SQLIdentifierExpr)) {
			SQLIdentifierExpr owner = null;
			String colunmName = null;
			
			SQLExpr expr = where.getLeft();
			if (expr instanceof SQLPropertyExpr) {
				SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
				owner = (SQLIdentifierExpr) propertyExpr.getOwner();
				colunmName = propertyExpr.getName();
			} else if(expr instanceof SQLIdentifierExpr) {
				colunmName = ((SQLIdentifierExpr) expr).getName();
			}
			
			Class<?> clazz = findModel(tables, owner, colunmName);
			if (clazz != null) {
				Table table = clazz.getAnnotation(Table.class);
				String name = table.value() + TOKEN + where.getRight();
				
				Object model = DATABASE_CACHE.get(name, clazz);
				for (SQLUpdateSetItem item : items) {
					SQLExpr column = item.getColumn();
					Object value = getValue(item.getValue());
					if (column instanceof SQLPropertyExpr) {
						SQLPropertyExpr property = (SQLPropertyExpr) column;
						if (property.getOwner().equals(owner)) {
							String field = ReflectionUtils.findField(clazz, property.getName());
							ReflectionUtils.setValue(model, field, value);
						}
					} else if (column instanceof SQLIdentifierExpr){
						SQLIdentifierExpr property = (SQLIdentifierExpr) column;
						String field = ReflectionUtils.findField(clazz, property.getName());
						ReflectionUtils.setValue(model, field, value);
					}
				}
				
				System.out.println(JSON.toJSONString(model));
			}
		} else {
			/** 生成select语句 */
			MySqlSelectQueryBlock queryBlock = new MySqlSelectQueryBlock();
			
			SQLTableSource from = null;
			SQLJoinTableSource join = null;
			
			List<SQLSelectItem> list = queryBlock.getSelectList();
			for (int i = 0, n = tables.size(); i < n; i++) {
				SQLExprTableSource source = tables.get(i);
				if (i == 0) {
					from = source;
				} else {
					if (join == null) {
						join = new SQLJoinTableSource();
						join.setJoinType(JoinType.COMMA);
						join.setLeft(from);
						from = join;
					}
					if (i + 1 < n) {
						SQLJoinTableSource right = new SQLJoinTableSource();
						right.setJoinType(JoinType.COMMA);
						right.setLeft(source);
						join.setRight(right);
						join = right;
					} else {
						join.setRight(source);
					}
				}
				String tableName = ((SQLIdentifierExpr) source.getExpr()).getName();
				Class<?> clazz = TABLE_MODEL_MAP.get(tableName);
				if (clazz != null) {
					String name = clazz.getAnnotation(Table.class).key();
					SQLExpr owner = new SQLIdentifierExpr(tableName);
					list.add(new SQLSelectItem(new SQLPropertyExpr(owner, name), tableName));
				}
			}
			queryBlock.setFrom(from);
			queryBlock.setWhere(where);
			
			StringBuffer sql = new StringBuffer();
			queryBlock.output(sql);
			
			
			@SuppressWarnings("rawtypes")
			List<Map> result = sqlClient.queryList(sql.toString(), Map.class);
			
			
			System.out.println(result);
		}
		
		System.out.println("the end");
	}
	
	public static Object getValue(SQLExpr expr) {
		if (expr instanceof SQLCharExpr) {
			return ((SQLCharExpr) expr).getText();
		} else if (expr instanceof SQLIntegerExpr) {
			return ((SQLIntegerExpr) expr).getNumber();
		} 
		return null;
	}
	
	
	
	
	/**
	 * 查询
	 * @param tables		数据库表列表
	 * @param ownerName		表名称
	 * @param colunmName	字段名
	 * @return String
	 * @throws SQLException 
	 */
	public static Class<?> findModel(List<SQLExprTableSource> tables, SQLIdentifierExpr owner,
			String colunmName) throws SQLException {
		Stack<Class<?>> result = new Stack<Class<?>>();
		for (SQLExprTableSource table : tables) {
			SQLIdentifierExpr expr = (SQLIdentifierExpr) table.getExpr();
			if (owner != null) {
				if (owner.equals(table.getExpr()) || owner.getName().equals(table.getAlias())) {
					Class<?> clazz = TABLE_MODEL_MAP.get(expr.getName());
					if (clazz != null && clazz.getAnnotation(Table.class).key().equals(colunmName))
						return clazz;
					return null;
				}
				continue;
			}
			Class<?> clazz = TABLE_MODEL_MAP.get(expr.getName());
			if (clazz.getAnnotation(Table.class).key().equals(colunmName))
				result.add(clazz);
		}
		if (result.size() > 1)
			throw new SQLException("[Err] 1052 - Column '" + colunmName + "' in where clause is ambiguous");
		return result.size() == 1 ? result.pop() : null;
	}
	
	public static void init() {
		TABLE_MODEL_MAP.put("t06_member_info", Member.class);
		TABLE_MODEL_MAP.put("t06_equity", Equity.class);
		/*
		Member member = new Member();
		member.setId(139);
		member.setEmail(null);
		member.setUsername("zx16589042");
		member.setPassword("123456");
		member.setRegisterTime(new Date());
		
		String key = getKey(member);
		if (key != null) {
			DATABASE_CACHE.set(getKey(member), member);
		}*/
	}
	
	
	/*public static String getKey(Object model) {
		Class<?> clazz = model.getClass();
		if (clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			Object key = ReflectionUtils.getValue(model, table.key());
			if (key != null)
				return table.value() + TOKEN + key;
		}
		return null;
	}*/
	
	public static List<SQLExprTableSource> getTables(SQLTableSource tableSource) {
		List<SQLExprTableSource> list = new ArrayList<SQLExprTableSource>();
		
		if (tableSource instanceof SQLJoinTableSource) {
			SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
			list.addAll(getTables(joinTableSource.getLeft()));
			list.addAll(getTables(joinTableSource.getRight()));
		} else if (tableSource instanceof SQLExprTableSource) {
			list.add((SQLExprTableSource) tableSource);
		}
		
		return list;
	}
}
