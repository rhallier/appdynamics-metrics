package org.appdynamics.metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



public class Metrics {

	public static void main(String[] args) throws Exception {
		
		ProgramArguments pgArgs = new ProgramArguments(args);

		if(pgArgs.noArgs())
			displayUsage();

		// Settings
		String username = pgArgs.getParameter("username", "root");
		String password = pgArgs.getParameter("password", "pwd");
		int port = Integer.parseInt(pgArgs.getParameter("port", "3388"));
		String hostname = pgArgs.getParameter("hostname", "localhost");
		String db = pgArgs.getParameter("db", "controller");
		String jdbcUrl="jdbc:mysql://" + hostname + ":" + port + "/" + db + "?" + "user=" + username + "&password=" + password;

		// JDBC driver loading
		Class.forName("com.mysql.jdbc.Driver").newInstance();

		Connection conn = null;

		try {

			if(pgArgs.getArgument(0).equals("applications")) {
				conn = DriverManager.getConnection(jdbcUrl);
				applications(conn);
			}
			else if(pgArgs.getArgument(0).equals("tiers")) {
				conn = DriverManager.getConnection(jdbcUrl);
				tiers(conn);
			}
			else if(pgArgs.getArgument(0).equals("metrics")) {
				if(pgArgs.getArgument(1)!=null) {
					if(pgArgs.getArgument(1).equals("list")) {
						conn = DriverManager.getConnection(jdbcUrl);
						listMetrics(conn, pgArgs.getArgument(2), db);
					}
					else if (pgArgs.getArgument(1).equals("delete") && pgArgs.getArgument(2)!=null) {
						conn = DriverManager.getConnection(jdbcUrl);
						deleteMetrics(conn, pgArgs.getArgument(2), db);
					}
					else
						displayUsage();
				}
				else
					displayUsage();
			}
			else
				displayUsage();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {} // ignore
			}
		}
	}

	private static void displayUsage() {
		System.out.println("Usage : [-hostname=] [-port=] [-username=] [-password=] applications|tiers|metrics list [filter]|metrics delete filter");
		System.exit(0);
	}
	
	private static void applications(Connection conn) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		
		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT id, name FROM application");

		while (rs.next()) {
			long id = rs.getLong(1);
			String name = rs.getString(2);

			System.out.println("id=" + id + ", name=" + name);
		}
	}

	private static void tiers(Connection conn) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		
		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT application_id,id, name FROM application_component");

		while (rs.next()) {
			long app_id = rs.getLong(1);
			long id = rs.getLong(2);
			String name = rs.getString(3);

			System.out.println("app_id=" + app_id+ ", id=" + id + ", name:" + name);
		}
	}

	private static void listMetrics(Connection conn, String filter, String db) throws SQLException {
		for(Metric m : findMetrics(conn, filter, db))
			System.out.println("Deletable="+(!m.isReferenced() ? "true ":"false")+", app_id=" + m.getApp_id()+ ", id=" + m.getId() + ", name=" + m.getName());
	}

	private static Collection<Metric> findMetrics(Connection conn, String filter, String db) throws SQLException {
		Collection<Metric> metrics = new LinkedList<>();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		stmt = conn.createStatement();
		String sql = "SELECT application_id,id, name,"+computeIsReferencedMetric(conn, db, "metric")+" FROM metric";
		if(filter!=null)
			sql=sql+" where name like '%"+filter+"%'";
		rs = stmt.executeQuery(sql);

		while (rs.next()) {
			long app_id = rs.getLong(1);
			long id = rs.getLong(2);
			String name = rs.getString(3);
			boolean isReferenced = rs.getBoolean(4);

			metrics.add(new Metric(id, app_id, name, isReferenced));
		}
		
		return metrics;
	}

	private static void deleteMetrics(Connection conn, String filter, String db) throws Exception {
		
		System.out.println("Finding metrics ...");
		
		Collection<Metric> metrics = findMetrics(conn, filter, db);
		
		if(!metrics.isEmpty()) {
			try {
				System.out.println("Deleting metrics ...");

				conn.setAutoCommit(false);
	
				PreparedStatement ps_metric_config_map = conn.prepareStatement("delete from metric_config_map where metric_id=?");
				PreparedStatement ps_metric = conn.prepareStatement("delete from metric where id=?");

				for(Metric metric : metrics) {
					
					if(!metric.isReferenced()) {
						ps_metric_config_map.setLong(1, metric.getId());
						ps_metric_config_map.executeUpdate();
						ps_metric.setLong(1, metric.getId());
						ps_metric.executeUpdate();
						conn.commit();
						System.out.println("Deleting : "+ metric);
					}
				}
			}
			catch(Exception e) {
				conn.rollback();
				throw e;
			}
		}
		else 
			System.out.println("No metrics to delete ...");

	}
	
	private static String computeIsReferencedMetric(Connection conn, String db, String referencedTable) throws SQLException {
		List<ForeignKey> foreignKeys = metricDependencyTables(conn, db, referencedTable, Collections.singletonList("metric_config_map"));
		
		StringBuilder sb = new StringBuilder();
		
		for(ForeignKey fk : foreignKeys) {
			if(sb.length()!=0) {
				sb.append(" or ");
			}
			
			sb.append("exists(select 1 from "+fk.tableName+" where "+fk.columnName+"="+referencedTable+".id)");
		}
		return sb.toString();
	}

	private static List<ForeignKey> metricDependencyTables(Connection conn, String db, String referencedTable, List<String> tablesToExclude) throws SQLException {
		String subsql = "select '"+db+"', table_name,constraint_name from information_schema.REFERENTIAL_CONSTRAINTS where CONSTRAINT_SCHEMA='"+db+"' and REFERENCED_TABLE_NAME='"+referencedTable+"'";
		
		if(tablesToExclude!=null && !tablesToExclude.isEmpty())
			for(String tableToExclude : tablesToExclude)
				subsql=subsql + "and TABLE_NAME!='"+tableToExclude+"'";
		
		String sql = "select table_name,column_name from information_schema.key_column_usage where (table_schema,table_name,constraint_name) in ("+subsql+")";

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
	
	static class Metric {
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

	static class ForeignKey {
		String columnName;
		String tableName;

		public ForeignKey(String tableName, String columnName) {
			super();
			this.tableName = tableName;
			this.columnName = columnName;
		}
	}
	
	static class ProgramArguments {
		List<String> arguments = new ArrayList<String>();
		Map<String, String> parameters = new HashMap<String,String>();
		
		ProgramArguments(String[] args) {
			if(args!=null && args.length!=0) {
				for(String arg : args) {
					if(arg==null)
						continue;
					if(arg.startsWith("-") && arg.contains("=")) {
						String[] splits = arg.substring(1).split("=");
						parameters.put(splits[0], splits[1]);
					}
					else 
						arguments.add(arg);
				}
			}
		}
		
		public boolean noArgs() {
			return arguments.isEmpty();
		}

		String getArgument(int index) {
			return (index<arguments.size()) ? arguments.get(index) : null;
		}

		String getParameter(String key) {
			return parameters.get(key);
		}

		String getParameter(String key, String defaultValue) {
			return parameters.containsKey(key) ? parameters.get(key) : defaultValue;
		}

	}
}
