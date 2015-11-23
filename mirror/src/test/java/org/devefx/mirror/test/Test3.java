package org.devefx.mirror.test;

import java.sql.SQLException;

import org.devefx.mirror.model.Member;
import org.devefx.mirror.utils.ReflectionUtils;

import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcUtils;

public class Test3 {
	public static void main(String[] args){
		
		String sql = "select a.id,name,main_picture,description,limit_amount,collect_amount,audit_status, " +
				"(collect_amount / limit_amount * 100) as collectPercent,(limit_day-(datediff(now(), audit_time))) as remainDay, if(b.id > 0, 1, 0) exist_lead " +
				"from t06_equity a right join t06_equity_leadinvestor b on a.id > b.equity_id " +
				"where a.member_id = ? and audit_status in (2,3,4,5,6) limit ?,?";
		
		sql = "select * from t06_equity, t06_member_info b where a.id = b.id";
		
		//sql = "select a.* from t06_member_info a LEFT JOIN t06_equity b on a.id = b.member_id, t06_member_focus c where a.id = c.member_id";
		
		SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcUtils.MYSQL);
		SQLSelectStatement selectStatement = parser.parseSelect();
		SQLSelect sqlSelect = selectStatement.getSelect();
		MySqlSelectQueryBlock selectQuery = (MySqlSelectQueryBlock) sqlSelect.getQuery();
		
		from(selectQuery.getFrom());
		
		
		System.out.println();
		
		System.out.println("查询的字段：" + selectQuery.getSelectList());
		/*System.out.println("查询的表：" + selectQuery.getFrom());
		System.out.println("查询的条件：" + selectQuery.getWhere());
		Limit limit = selectQuery.getLimit();
		System.out.println("数据分页：" + limit.getOffset() + ", " + limit.getRowCount());
		*/
		testMongodb();
	}
	
	public static void from(SQLTableSource tableSource) {
		// select * from table1 a left jion table2 b on a.id = b.id
		if (tableSource instanceof SQLJoinTableSource) {
			SQLJoinTableSource joinTable = (SQLJoinTableSource) tableSource;
			
			if (joinTable.getLeft() instanceof SQLJoinTableSource) {
				from(joinTable.getLeft());
			} else {
				SQLExprTableSource leftTable = conver(joinTable.getLeft());			// 左表
				SQLIdentifierExpr lTable = conver(leftTable.getExpr());
				System.out.println(String.format("左表：%s.%s", leftTable.getAlias(), lTable.getName()));
			}
			
			if (joinTable.getRight() instanceof SQLJoinTableSource) {
				from(joinTable.getRight());
			} else {
				SQLExprTableSource rightTable = conver(joinTable.getRight());		// 右表
				SQLIdentifierExpr rTable = conver(rightTable.getExpr());
				System.out.println(String.format("右表：%s.%s", rightTable.getAlias(), rTable.getName()));
			}
			
			SQLBinaryOpExpr joinCondition = (SQLBinaryOpExpr) joinTable.getCondition();
			if (joinCondition != null) {
				SQLPropertyExpr leftProperty = conver(joinCondition.getLeft());		// 左字段
				SQLIdentifierExpr lpTable = conver(leftProperty.getOwner());		// 左字段所在表
				SQLPropertyExpr rightProperty = conver(joinCondition.getRight());	// 右字段
				SQLIdentifierExpr rpTable = conver(rightProperty.getOwner());		// 左字段所在表
				SQLBinaryOperator binaryOperator = joinCondition.getOperator();		// 比较符
				
				System.out.println(String.format("条件：%s.%s %s %s.%s", lpTable.getName(), leftProperty.getName(), 
						binaryOperator.name, rpTable.getName(), rightProperty.getName()));
			}
			System.out.println("联接类型：" + joinTable.getJoinType().name);
		} else if (tableSource instanceof SQLExprTableSource) {
			SQLExprTableSource table = (SQLExprTableSource) tableSource;
			SQLIdentifierExpr t = conver(table.getExpr());
			System.out.println(String.format("表名：%s.%s", table.getAlias(), t.getName()));
		}
	}
	
	public static<T> T conver(Object object) {
		return object != null ? (T) object : null;
	}
	
	
	public static void testMongodb() {
		Member member = new Member();
		
		ReflectionUtils.setValue(member, "username", "123456");
		
		System.out.println(null instanceof Member);
		
		System.out.println(member.getUsername());
	}
	
}
class SQLTable {
	public String alias;
	public String name;
}
