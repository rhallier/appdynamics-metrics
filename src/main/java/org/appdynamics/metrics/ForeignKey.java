package org.appdynamics.metrics;

class ForeignKey {
	String columnName;
	String tableName;

	public ForeignKey(String tableName, String columnName) {
		super();
		this.tableName = tableName;
		this.columnName = columnName;
	}
}