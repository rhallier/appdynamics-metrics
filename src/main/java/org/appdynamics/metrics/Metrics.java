package org.appdynamics.metrics;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Metrics {

	public static void main(String[] args) throws Exception {

		ProgramArguments pgArgs = new ProgramArguments(args);

		if (pgArgs.noArgs())
			displayUsage();

		// Settings
		String username = pgArgs.getParameter("username", "root");
		String password = pgArgs.getParameter("password", "pwd");
		int port = Integer.parseInt(pgArgs.getParameter("port", "3388"));
		String hostname = pgArgs.getParameter("hostname", "localhost");
		String db = pgArgs.getParameter("db", "controller");
		String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + db + "?" + "user=" + username + "&password=" + password;
		String filename = pgArgs.getParameter("filename", null);

		// JDBC driver loading
		Class.forName("com.mysql.jdbc.Driver").newInstance();

		Connection conn = null;
		PrinterAdapter out = new PrinterAdapter(filename);

		try {

			if (pgArgs.getArgument(0).equals("applications")) {
				conn = DriverManager.getConnection(jdbcUrl);
				applications(conn, out);
			} else if (pgArgs.getArgument(0).equals("tiers")) {
				conn = DriverManager.getConnection(jdbcUrl);
				tiers(conn, out);
			} else if (pgArgs.getArgument(0).equals("metrics-list")) {
				conn = DriverManager.getConnection(jdbcUrl);
				listMetrics(conn, pgArgs.getArgument(1), out, db);
			} else if (pgArgs.getArgument(0).equals("metrics-stats")) {
				conn = DriverManager.getConnection(jdbcUrl);
				int beforeMin = pgArgs.getArgument(1) !=null ? Integer.parseInt(pgArgs.getArgument(1)): 10;
				statsMetrics(conn, beforeMin, out, db);
			} else if (pgArgs.getArgument(0).equals("metrics-stats-summary")) {
				conn = DriverManager.getConnection(jdbcUrl);
				int beforeMin = pgArgs.getArgument(1) !=null ? Integer.parseInt(pgArgs.getArgument(1)): 10;
				statsMetricsSummary(conn, beforeMin, out, db);
			} else if (pgArgs.getArgument(0).equals("metrics-stats-storage")) {
				conn = DriverManager.getConnection(jdbcUrl);
				statsMetricsStorage(conn, pgArgs.getArgument(2), out, db);
			} else if (pgArgs.getArgument(0).equals("metrics-delete") && pgArgs.getArgument(1) != null) {
				conn = DriverManager.getConnection(jdbcUrl);
				deleteMetrics(conn, pgArgs.getArgument(1), out, db);
			} else
				displayUsage();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				} // ignore
			}
			
			if(out!=null)
				out.close();
		}
	}

	private static void displayUsage() {
		System.out.println("Usage : [-hostname=] [-port=] [-username=] [-password=] [-filename=] applications|tiers|metrics-list [filter]|metrics-stats-storage|metrics-stats|metrics-stats-summary [beforeMin]|metrics-delete filter");
		System.exit(0);
	}

	private static void applications(Connection conn, PrinterAdapter out) throws SQLException, FileNotFoundException {
		Statement stmt = null;
		ResultSet rs = null;
		 
		out.console();

		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT id, name FROM application");

		out.console();

		while (rs.next()) {
			long id = rs.getLong(1);
			String name = rs.getString(2);

			out.println("id=" + id + ", name=" + name);
		}
	}

	private static void tiers(Connection conn, PrinterAdapter out) throws SQLException, FileNotFoundException {
		Statement stmt = null;
		ResultSet rs = null;

		out.console();

		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT application_id,id, name FROM application_component");

		out.console();

		while (rs.next()) {
			long app_id = rs.getLong(1);
			long id = rs.getLong(2);
			String name = rs.getString(3);

			out.println("app_id=" + app_id + ", id=" + id + ", name:" + name);
		}
	}

	private static void listMetrics(Connection conn, String filter, PrinterAdapter out, String db) throws SQLException, FileNotFoundException {
		String header = "Deletable;ApplicationId;MetricId;MetricName;";
		String row = "%s;%d;%d;\"%s\";";

		out.println(header);

		for (Metric m : findMetrics(conn, filter, out, db))
			out.println(String.format(row, !m.isReferenced() ? "true" : "false", m.getApp_id(), m.getId(), m.getName()));
	}

	private static void statsMetrics(Connection conn, int beforeMin, PrinterAdapter out, String db) throws SQLException, FileNotFoundException {

		Collection<MetricStat> stats = findMetricStats(conn, beforeMin, out, db);
		
		String header = "Timestamp;AccountName;ApplicationName;TierName;NodeName;AgentType;MetricsCount;";
		String row = "\"%tc\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";%d;";
		
		out.println(header);
		
		for (MetricStat m : stats)
			out.println(String.format(row, m.getTimestamp(), m.getAccountName(), m.getApplicationName(), m.getTierName(), m.getNodeName(), m.getModifiedAgentType(), m.getMetricsCount()));
	}

	private static void statsMetricsSummary(Connection conn, int beforeMin, PrinterAdapter out, String db) throws SQLException, FileNotFoundException {

		Collection<MetricStat> stats = findMetricStats(conn, beforeMin, out, db);
		
		// Group by AgentType
		Map<String, List<MetricStat>> agentTypes = new HashMap<String, List<MetricStat>>();
		for(MetricStat ms : stats) {
			List<MetricStat> s = agentTypes.get(ms.getModifiedAgentType());
			
			if(s==null) {
				s = new LinkedList<MetricStat>();
				agentTypes.put(ms.getModifiedAgentType(), s);
			}
			
			s.add(ms);
		}
		
		// Compute Sum, Avg, Count, Min and Max
		Map<String, GroupOperator> operators = new HashMap<String, GroupOperator>();
		
		for(String agentType : agentTypes.keySet()) {
			GroupOperator go = null;
			
			for(MetricStat ms : agentTypes.get(agentType)) {
				if(go==null) {
					go = new GroupOperator(agentType, ms.getTimestamp());
					operators.put(agentType, go);
				}
				go.update(ms.getMetricsCount());
			}
		}

		// Display
		String header = "Timestamp;AgentType;Min;Avg;Max;Sum;Count;";
		String row = "\"%tc\";\"%s\";%d;%d;%d;%d;%d;";
		
		out.println(header);
		
		for (GroupOperator go : operators.values())
			out.println(String.format(row, go.timestamp, go.agentType, go.min, go.getAvg(), go.max, go.sum, go.count));
	}

	private static void statsMetricsStorage(Connection conn, String filter, PrinterAdapter out, String db) throws SQLException, FileNotFoundException {

		Collection<MetricStorageStat> stats = findMetricStatsStorage(conn, filter, out, db);
		
		String header = "Table;Count;SizeMB;AvgRowLength";
		String row = "\"%s\";%d;%d;%d;";
		
		out.println(header);
		
		for (MetricStorageStat m : stats)
			out.println(String.format(row, m.getTable(), m.getCount(), m.getSizeMB(), m.getAvgRowLength()));
	}

	private static Collection<Metric> findMetrics(Connection conn, String filter, PrinterAdapter out, String db) throws SQLException {
		Collection<Metric> metrics = new LinkedList<>();

		Statement stmt = null;
		ResultSet rs = null;

		out.console();

		stmt = conn.createStatement();
		String sql = "SELECT application_id,id, name," + computeIsReferencedMetric(conn, db, "metric") + " FROM metric";
		if (filter != null)
			sql = sql + " where name like '%" + filter + "%'";
		rs = stmt.executeQuery(sql);

		out.console();

		while (rs.next()) {
			long app_id = rs.getLong(1);
			long id = rs.getLong(2);
			String name = rs.getString(3);
			boolean isReferenced = rs.getBoolean(4);

			metrics.add(new Metric(id, app_id, name, isReferenced));
			out.console();
		}

		return metrics;
	}

	private static Collection<MetricStat> findMetricStats(Connection conn, int beforeMin, PrinterAdapter out, String db) throws SQLException {
		Collection<MetricStat> metricStats = new LinkedList<>();

		Statement stmt = null;
		ResultSet rs = null;

		out.console();
		
		stmt = conn.createStatement();
		String sql = "select from_unixtime(mdm.ts_min*60), acc.name, app.name as application, tier.name as tier, node.name as node, m.agent_type, count(*) from metricdata_min mdm join metric m on m.id=mdm.metric_id join application app on app.id=m.application_id join account acc on app.account_id=acc.id left join application_component_node node on node.id=mdm.node_id left join application_component tier on tier.id=node.application_component_id where ts_min = (select max(ts_min) - "+beforeMin+" from metricdata_min) group by 1, 2, 3, 4, 5, 6 order by 1";
		rs = stmt.executeQuery(sql);

		out.console();

		while (rs.next()) {
			Date timestamp = rs.getDate(1);
			String accountName = rs.getString(2);
			String applicationName = rs.getString(3);
			String tierName = rs.getString(4);
			String nodeName = rs.getString(5);
			String agentType = rs.getString(6);
			int metricsCount = rs.getInt(7);

			metricStats.add(new MetricStat(timestamp, accountName, applicationName, tierName, nodeName, agentType, metricsCount));
			out.console();
		}

		return metricStats;
	}

	private static Collection<MetricStorageStat> findMetricStatsStorage(Connection conn, String filter, PrinterAdapter out, String db) throws SQLException {
		Collection<MetricStorageStat> metricStorageStats = new ArrayList<MetricStorageStat>();

		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME, ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), TABLE_ROWS, AVG_ROW_LENGTH FROM information_schema.TABLES order by 2 desc");
		long size=0,count=0;
		int avgRowLength=0;
		String tableName;
		
		while(rs.next()) {
			tableName = rs.getString(1);
			size = rs.getLong(2);
			count = rs.getLong(3);
			avgRowLength = rs.getInt(4);
			
			metricStorageStats.add(new MetricStorageStat(tableName, size, count, avgRowLength));
		}
		
		stmt.close();

		return metricStorageStats;
	}

	private static void deleteMetrics(Connection conn, String filter, PrinterAdapter out, String db) throws Exception {

		System.out.println("Retrieving metrics ...");

		Collection<Metric> metrics = findMetrics(conn, filter, out, db);

		if (!metrics.isEmpty()) {
			try {
				System.out.println("Deleting metrics ...");

				conn.setAutoCommit(false);

				PreparedStatement ps_metric_config_map = conn.prepareStatement("delete from metric_config_map where metric_id=?");
				PreparedStatement ps_metric = conn.prepareStatement("delete from metric where id=?");

				for (Metric metric : metrics) {

					if (!metric.isReferenced()) {
						ps_metric_config_map.setLong(1, metric.getId());
						ps_metric_config_map.executeUpdate();
						ps_metric.setLong(1, metric.getId());
						ps_metric.executeUpdate();
						conn.commit();
						out.println("Deleting : " + metric);
					}

					out.console();
				}
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} else
			System.out.println("No metrics to delete ...");

	}

	private static String computeIsReferencedMetric(Connection conn, String db, String referencedTable) throws SQLException {
		List<ForeignKey> foreignKeys = metricDependencyTables(conn, db, referencedTable, Collections.singletonList("metric_config_map"));

		StringBuilder sb = new StringBuilder();

		for (ForeignKey fk : foreignKeys) {
			if (sb.length() != 0) {
				sb.append(" or ");
			}

			sb.append("exists(select 1 from " + fk.tableName + " where " + fk.columnName + "=" + referencedTable + ".id)");
		}
		return sb.toString();
	}

	private static List<ForeignKey> metricDependencyTables(Connection conn, String db, String referencedTable, List<String> tablesToExclude) throws SQLException {
		String subsql = "select '" + db + "', table_name,constraint_name from information_schema.REFERENTIAL_CONSTRAINTS where CONSTRAINT_SCHEMA='" + db + "' and REFERENCED_TABLE_NAME='" + referencedTable + "'";

		if (tablesToExclude != null && !tablesToExclude.isEmpty())
			for (String tableToExclude : tablesToExclude)
				subsql = subsql + "and TABLE_NAME!='" + tableToExclude + "'";

		String sql = "select table_name,column_name from information_schema.key_column_usage where (table_schema,table_name,constraint_name) in (" + subsql + ")";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);

		List<ForeignKey> result = new ArrayList<ForeignKey>();
		while (rs.next()) {
			String tableName = rs.getString(1);
			String columnName = rs.getString(2);
			result.add(new ForeignKey(tableName, columnName));
		}

		return result;
	}

	static class ProgramArguments {
		List<String> arguments = new ArrayList<String>();
		Map<String, String> parameters = new HashMap<String, String>();

		ProgramArguments(String[] args) {
			if (args != null && args.length != 0) {
				for (String arg : args) {
					if (arg == null)
						continue;
					if (arg.startsWith("-") && arg.contains("=")) {
						int index = arg.indexOf("=");
						String action = arg.substring(1,index);
						String value = arg.substring(index+1);
						parameters.put(action, value);
					} else
						arguments.add(arg);
				}
			}
		}

		public boolean noArgs() {
			return arguments.isEmpty();
		}

		String getArgument(int index) {
			return (index < arguments.size()) ? arguments.get(index) : null;
		}

		String getParameter(String key) {
			return parameters.get(key);
		}

		String getParameter(String key, String defaultValue) {
			return parameters.containsKey(key) ? parameters.get(key) : defaultValue;
		}
	}

	static class GroupOperator {
		public String agentType;
		public Date timestamp;
		public long count;
		public long sum;
		public long min=-1;
		public long max;
		
		public GroupOperator(String agentType, Date timestamp) {
			super();
			this.agentType = agentType;
			this.timestamp = timestamp;
		}

		void update(long value) {
			if(value>max)
				max=value;
			if(min<0 || value<min)
				min=value;
			sum+=value;
			count++;
		}
		
		long getAvg() {
			return (long)sum/count;
		}
	}
}
