package org.appdynamics.metrics;

class Metric {
	private long id;
	private long app_id;
	private String name;
	private boolean isReferenced;

	public Metric(long id, long app_id, String name, boolean isReferenced) {
		super();
		this.id = id;
		this.app_id = app_id;
		this.name = name;
		this.isReferenced = isReferenced;
	}

	public long getId() {
		return id;
	}

	public long getApp_id() {
		return app_id;
	}

	public String getName() {
		return name;
	}

	public boolean isReferenced() {
		return isReferenced;
	}

	@Override
	public String toString() {
		return "Metric [id=" + id + ", app_id=" + app_id + ", name=" + name + ", isReferenced=" + isReferenced + "]";
	}
}