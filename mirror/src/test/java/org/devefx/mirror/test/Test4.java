package org.devefx.mirror.test;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;

import org.devefx.mirror.model.Equity;
import org.devefx.mirror.model.Member;
import org.devefx.mirror.sqlmap.client.SqlMapClient;
import org.devefx.mirror.sqlmap.engine.builder.xml.ConfigParser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;

import com.alibaba.fastjson.JSON;

public class Test4 {
	
	public static void main(String[] args) throws SQLException {
		// init mirror
		ClassLoader loader = Test4.class.getClassLoader();
		InputStream is = loader.getResourceAsStream("SqlConfiguration.xml");
		
		ConfigParser configParser = new ConfigParser();
		final SqlMapClient sqlMapClient = configParser.parse(is);
		
		/*Member member = new Member();
		member.setId(1);
		member.setEmail("yyq8092@163.com");
		member.setUsername("yyq8092");
		member.setPassword("123456");
		member.setRegisterTime(new Date());
		
		sqlMapClient.update(member);
		sqlMapClient.insert(member);*/
		
		// init ibatis
		URL url = loader.getResource("sqlmap.xml");
		FileSystemResource fileSystemResource = new FileSystemResource(url.getFile());
		
		SqlMapClientFactoryBean clientFactoryBean = new SqlMapClientFactoryBean();
		clientFactoryBean.setDataSource(sqlMapClient.getDataSource());
		clientFactoryBean.setConfigLocation(fileSystemResource);
		try {
			clientFactoryBean.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		final com.ibatis.sqlmap.client.SqlMapClient mapClient = clientFactoryBean.getObject();
		
		Equity equity = sqlMapClient.query(Equity.class, 140);
		System.out.println(JSON.toJSONString(equity));
		
		equity = (Equity) mapClient.queryForObject("getEquityById", 140);
		System.out.println(JSON.toJSONString(equity));
		
		
		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				long t0 = System.currentTimeMillis();
				try {
					for (int i = 0; i < 10000; i++) {
						Equity equity = sqlMapClient.query(Equity.class, 140);
						//sqlMapClient.query(Member.class, 1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				long t1 = System.currentTimeMillis();
				System.out.println("mirror:" + (t1 - t0));
			}
		});
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				long t2 = System.currentTimeMillis();
				try {
					for (int i = 0; i < 10000; i++) {
						Equity equity = (Equity) mapClient.queryForObject("getEquityById", 140);
						//mapClient.queryForObject("getMemberById", 1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				long t3 = System.currentTimeMillis();
				System.out.println("ibatis:" + (t3 - t2));
			}
		});
		thread1.start();
		thread2.start();
		
		System.out.println("\nthe end");
	}
}
