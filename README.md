# AppDynamics Metrics Tool

A simple tool to list and delete metrics, and report stats about metrics

##Installation
To build from source, clone this repository and run 'gradlew shadowJar'. This will produce a metrics-VERSION-all.jar in the build/libs directory. Alternatively, download the latest release archive from [Github](https://github.com/rhallier/appdynamics-metrics/releases).

##Usage

### Help 

    java -jar metrics-VERSION-jar-with-dependencies.jar
    
    Usage : [-hostname=] [-port=] [-username=] [-password=] [-filename=] applications|tiers|metrics-list [filter]|metrics-delete filter|metrics-stats|metrics-stats-storage

### List metrics 

List all the metrics (default : mysql root@localhost:3388) 

    java -jar metrics-VERSION-all.jar -password=*** metrics-list 
  
    Deletable;ApplicationId;MetricId;MetricName;
    ...
    true;5;30346;"BTM|Application Summary|Component:8|Average Response Time (ms)";
    true;5;30347;"BTM|Application Summary|Component:8|Calls per Minute";
    true;5;30348;"BTM|Application Summary|Component:8|Errors per Minute";
    ...

List all the metrics containing the keyword 'JMX' (default : mysql root@localhost:3388)

    java -jar metrics-VERSION-all.jar -password=*** metrics-list JMX
    
    Deletable;ApplicationId;MetricId;MetricName;
    true;5;30370;"Server|Component:8|JMX|Sessions|activeSessions";
    true;5;30371;"Server|Component:8|JMX|Sessions|expiredSessions";
    true;5;30372;"Server|Component:8|JMX|Sessions|maxActive";
    ...
	
### Delete metrics 

__Warning : Please do a db backup before, there is no rollback operation, so please double-check your delete query !!!!__ 

You must stop the controller (./controller.sh stop-appserver), not the db

Delete all the metrics containing '|ECommerce-Services|JMX|JDBC Connection Pools|' (default : mysql root@localhost:3388)

    java -jar metrics-VERSION-all.jar -password=*** metrics-delete "|ECommerce-Services|JMX|JDBC Connection Pools|"

Restart the controller (./controller.sh start-appserver)
    

### Metrics stats
 
Count (default : mysql root@localhost:3388) 

    java -jar metrics-VERSION-all.jar -password=*** metrics-stats

    Timestamp;AccountName;ApplicationName;TierName;NodeName;AgentType;MetricsCount
    Thu Aug 24 00:00:00 CEST 2017;customer1;MyApp1;MyTier1;MyNode1;MACHINE_AGENT_LEGACY;43
    Thu Aug 24 00:00:00 CEST 2017;customer1;MyApp2;MyTier2;Mu=yNode2;APP_AGENT;66
    ...

Summary (default : mysql root@localhost:3388) 

    java -jar metrics-VERSION-all.jar -password=*** metrics-stats-summary

	Timestamp;AgentType;Min;Avg;Max;Sum;Count;
	"Mon Feb 12 00:00:00 CET 2018";"APP_AGENT";103;227;2196;205434;902;
	"Mon Feb 12 00:00:00 CET 2018";"MACHINE_AGENT_LEGACY";64;84;92;4876;58;
	"Mon Feb 12 00:00:00 CET 2018";"EUM";890;2372;9641;21356;9;
    ...

Storage (default : mysql root@localhost:3388) 

    java -jar metrics-VERSION-all.jar -password=*** metrics-stats-storage
    
    Table;Count;SizeMB;AvgRowLength;
    ...
    "metricdata_hour";3422;8;226;
	...
	
##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.
