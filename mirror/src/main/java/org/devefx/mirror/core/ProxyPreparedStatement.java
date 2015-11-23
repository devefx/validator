package org.devefx.mirror.core;

import java.sql.PreparedStatement;

public interface ProxyPreparedStatement {
	PreparedStatement setProxy(PreparedStatement stmt);
	
	String getStaticSql();
}
