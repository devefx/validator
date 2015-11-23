package org.devefx.discard;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class PreparedStatementProxy implements MethodInterceptor {

    private PreparedStatement stmt;
    private PreparedStatement proxy;
    
    private String originalSql;
    private StringBuffer preparedSql;
    
    public PreparedStatementProxy(PreparedStatement stmt, String sql) {
    	this.stmt = stmt;
    	this.originalSql = sql;
    	try {
			resolve(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	/** create proxy object */
    	Enhancer enhancer = new Enhancer();
    	enhancer.setSuperclass(PreparedStatement.class);
    	enhancer.setCallback(this);
    	this.proxy = (PreparedStatement) enhancer.create();
	}
    
    private String[] params;
    
    private void resolve(String sql) throws SQLException {
    	DatabaseMetaData dbmd = stmt.getConnection().getMetaData();
    	String quotedIdentifierString = dbmd.getIdentifierQuoteString();
    	
    	char quotedIdentifierChar = 0;
    	
    	if ((quotedIdentifierString != null) && !quotedIdentifierString.equals(" ") && (quotedIdentifierString.length() > 0)) {
            quotedIdentifierChar = quotedIdentifierString.charAt(0);
        }
		
		int statementLength = sql.length();
		int paramNum = 0;
		boolean inQuotes = false;
		char quoteChar = 0;
		boolean inQuotedId = false;
		int i;
		
		preparedSql = new StringBuffer(statementLength);
		
		int statementStartPos = findStartOfStatement(sql);
		
		for (i = statementStartPos; i < statementLength; ++i) {
			char c = sql.charAt(i);
			
			if (c == '\\' && i < (statementLength - 1)) {
				i++;
                continue; // next character is escaped
			}
			
			if (!inQuotes && (quotedIdentifierChar != 0) && (c == quotedIdentifierChar)) {
				inQuotedId = !inQuotedId;
			} else {
				// only respect quotes when not in a quoted identifier
				if (inQuotes) {
					if (((c == '\'') || (c == '"')) && c == quoteChar) {
						if (i < (statementLength - 1) && sql.charAt(i + 1) == quoteChar) {
                            i++;
                            continue; // inline quote escape
                        }
						inQuotes = !inQuotes;
						quoteChar = 0;
					}
				} else {
					if (c == '#' || (c == '-' && (i + 1) < statementLength && sql.charAt(i + 1) == '-')) {
						// run out to end of statement, or newline, whichever comes first
						int endOfStmt = statementLength - 1;
						for (; i < endOfStmt; i++) {
                            c = sql.charAt(i);
                            if (c == '\r' || c == '\n') {
                                break;
                            }
                        }
						continue;
					} else if (c == '/' && (i + 1) < statementLength) {
						// Comment?
						char cNext = sql.charAt(i + 1);
						if (cNext == '*') {
							i += 2;
							for (int j = i; j < statementLength; j++) {
                                i++;
                                cNext = sql.charAt(j);
                                if (cNext == '*' && (j + 1) < statementLength) {
                                    if (sql.charAt(j + 1) == '/') {
                                        i++;
                                        if (i < statementLength) {
                                            c = sql.charAt(i);
                                        }
                                        break; // comment done
                                    }
                                }
                            }
						}
					// quotes?
					} else if ((c == '\'') || (c == '"')) {
                        inQuotes = true;
                        quoteChar = c;
                    }
				}
			}
			
			if ((c == '?') && !inQuotes && !inQuotedId) {
				preparedSql.append("/** PLACEHOLDER **/");
				paramNum ++;
				continue;
			}
			
			preparedSql.append(c);
		}
    	
		params = new String[paramNum];
    }

    private int findStartOfStatement(String sql) {
        int statementStartPos = 0;

        if (startsWithIgnoreCaseAndWs(sql, "/*", 0)) {
            statementStartPos = sql.indexOf("*/");

            if (statementStartPos == -1) {
                statementStartPos = 0;
            } else {
                statementStartPos += 2;
            }
        } else if (startsWithIgnoreCaseAndWs(sql, "--", 0) || startsWithIgnoreCaseAndWs(sql, "#", 0)) {
            statementStartPos = sql.indexOf('\n');

            if (statementStartPos == -1) {
                statementStartPos = sql.indexOf('\r');

                if (statementStartPos == -1) {
                    statementStartPos = 0;
                }
            }
        }

        return statementStartPos;
    }
	
    private boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
        if (searchIn == null) {
            return searchFor == null;
        }

        int inLength = searchIn.length();

        for (; beginPos < inLength; beginPos++) {
            if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
                break;
            }
        }
        return searchIn.regionMatches(true, beginPos, searchFor, 0, searchFor.length());
    }
    
	@Override
	public Object intercept(Object proxy, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("setObject".equals(method.getName())) {
			int index = (Integer) args[0] - 1;
			params[index] = args[1].toString();
		}
		return method.invoke(stmt, args);
	}
	
	public PreparedStatement getProxy() {
		return proxy;
	}
	public String getOriginalSql() {
		return originalSql;
	}
	
	public String getStaticSql() {
		StringBuffer staticSql = new StringBuffer();
		Pattern pattern = Pattern.compile("/\\*\\* PLACEHOLDER \\*\\*/");
		Matcher matcher = pattern.matcher(preparedSql);
		for (int i = 0; matcher.find(); i++) {
			matcher.appendReplacement(staticSql, params[i]);
		}
		matcher.appendTail(staticSql);
		return staticSql.toString();
	}
}
