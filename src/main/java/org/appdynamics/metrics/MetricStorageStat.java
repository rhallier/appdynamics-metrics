package org.appdynamics.metrics;

public class MetricStorageStat {
	public String table;
	public long sizeKB;
	public int count;

	public MetricStorageStat(String table, long sizeKB, int count) {
		super();
		this.table = table;
		this.sizeKB = sizeKB;
		this.count = count;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public long getSizeKB() {
		return sizeKB;
	}

	public void setSizeKB(long sizeKB) {
		this.sizeKB = sizeKB;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "MetricStorageStat [table=" + table + ", sizeKB=" + sizeKB + ", count=" + count + "]";
	}

}
