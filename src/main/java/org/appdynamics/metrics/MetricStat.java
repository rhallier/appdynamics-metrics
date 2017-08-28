package org.appdynamics.metrics;

import java.util.Date;

class MetricStat {
	private Date timestamp;
	private String accountName;
	private String applicationName;
	private String tierName;
	private String nodeName;
	private String agentType;
	private int metricsCount;

	public MetricStat(Date timestamp, String accountName, String applicationName, String tierName, String nodeName, String agentType, int metricsCount) {
		super();
		this.timestamp = timestamp;
		this.accountName = accountName;
		this.applicationName = applicationName;
		this.tierName = tierName;
		this.nodeName = nodeName;
		this.agentType = agentType;
		this.metricsCount = metricsCount;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getTierName() {
		return tierName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getAgentType() {
		return agentType;
	}

	public int getMetricsCount() {
		return metricsCount;
	}

	@Override
	public String toString() {
		return "MetricStat [timestamp=" + timestamp + ", accountName=" + accountName + ", applicationName=" + applicationName + ", tierName=" + tierName + ", nodeName=" + nodeName + ", agentType=" + agentType + ", metricsCount=" + metricsCount + "]";
	}

}