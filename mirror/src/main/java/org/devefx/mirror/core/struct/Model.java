package org.devefx.mirror.core.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.devefx.mirror.annotation.Table;
import org.devefx.mirror.core.struct.impl.EntityProperty;
import org.devefx.mirror.core.struct.impl.PrimitiveProperty;

public class Model {
	private static final String TOKEN = "#";
	private Class<?> modelClass;
	private String tableName;
	private String primaryKey;
	private Map<String, Property> columnProperty;
	
	public Model(Class<?> modelClass, Table table) {
		this.modelClass = modelClass;
		this.tableName = table.value();
		this.primaryKey = table.key();
		columnProperty = new HashMap<String, Property>();
	}
	
	public void addMapping(String column, Property property) {
		columnProperty.put(column, property);
	}
	
	public Class<?> getModelClass() {
		return modelClass;
	}
	public String getTableName() {
		return tableName;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	public Map<String, Property> getColumnProperty() {
		return columnProperty;
	}
	
	private String queryColumn;
	public String getQueryColumn() {
		if (queryColumn == null) {
			queryColumn = "";
			for (String column : columnProperty.keySet()) {
				queryColumn += (queryColumn != "" ? ", " : "") + column;
			}
		}
		return queryColumn;
	}
	public String getToken(Object key) {
		return tableName + TOKEN + key;
	}
	
	private String querySql;
	public String getQuerySql() {
		if (querySql == null) {
			querySql = new SQLSelect(this).getSql();
		}
		return querySql;
	}
	
	private class SQLSelect {
		private List<String> column;
		private List<String> from;
		private List<String> where;
		private Set<String> closeSet;
		private Model model;
		public SQLSelect(Model model) {
			this.column = new ArrayList<String>();
			this.from = new ArrayList<String>();
			this.where = new ArrayList<String>();
			this.closeSet = new HashSet<String>();
			this.model = model;
			process(model);
		}
		private void process(Model model) {
			from.add(model.getTableName());
			for (Map.Entry<String, Property> entry : model.columnProperty.entrySet()) {
				Property property = entry.getValue();
				if (property instanceof EntityProperty) {
					EntityProperty entityProperty = (EntityProperty) property;
					if (!entityProperty.isCollection()) {
						Model childModel = entityProperty.getModel();
						String addr = property.getClass() + "." + property.getName();
						if (childModel != null && !closeSet.contains(addr)) {
							closeSet.add(addr);
							StringBuffer sql = new StringBuffer();
							sql.append(model.getTableName() + "." + entry.getKey());
							sql.append(" = ");
							sql.append(childModel.getTableName() + "." + childModel.getPrimaryKey());
							where.add(sql.toString());
							process(childModel);
						}
					}
				} else if (property instanceof PrimitiveProperty) {
					column.add(model.getTableName() + "." + entry.getKey());
				}
			}
		}
		public String getSql() {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT ");
			for (int i = 0, n = column.size(); i < n; i++) {
				if (i != 0)
					sql.append(", ");
				sql.append(column.get(i));
			}
			sql.append(" FROM ");
			for (int i = 0, n = from.size(); i < n; i++) {
				if (i != 0)
					sql.append(", ");
				sql.append(from.get(i));
			}
			sql.append(" WHERE ");
			sql.append(model.getTableName() + "." + model.getPrimaryKey());
			sql.append(" = ?");
			for (int i = 0, n = where.size(); i < n; i++) {
				sql.append(" AND ");
				sql.append(where.get(i));
			}
			return sql.toString();
		}
	}
}


