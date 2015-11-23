package org.devefx.mirror.test;

import org.devefx.discard.ConfigParser;
import org.devefx.discard.SqlClient;
import org.devefx.mirror.model.Member;

import com.alibaba.fastjson.JSON;

public class Test2 {
	
	public static void main(String[] args) throws Exception {
		ConfigParser configParser = new ConfigParser("classpath:Configuration.xml");
		
		SqlClient sqlClient = configParser.getSqlClient();
		
		Member member = sqlClient.query("select * from t06_member_info where id = ?", Member.class, 1);
		System.out.println(JSON.toJSONString(member));
		
		sqlClient.update(member);
	}
}
