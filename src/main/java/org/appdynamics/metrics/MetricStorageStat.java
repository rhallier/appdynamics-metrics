package org.appdynamics.metrics;

public class MetricStorageStat {
	public String table;
	public long sizeMB;
	public long count;
	public int avgRowLength;

	public MetricStorageStat(String table, long sizeKB, long count, int avgRowLength) {
		super();
		this.table = table;
		this.sizeMB = sizeKB;
		this.count = count;
		this.avgRowLength = avgRowLength;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public long getSizeMB() {
		return sizeMB;
	}

	public void setSizeMB(long sizeMB) {
		this.sizeMB = sizeMB;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getAvgRowLength() {
		return avgRowLength;
	}

	public void setAvgRowLength(int avgRowLength) {
		this.avgRowLength = avgRowLength;
	}

	@Override
	public String toString() {
		return "MetricStorageStat [table=" + table + ", sizeMB=" + sizeMB + ", count=" + count + ", avgRowLength=" + avgRowLength + "]";
	}
}
