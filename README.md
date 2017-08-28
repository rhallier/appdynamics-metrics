# AppDynamics Metrics Tool

A simple tool to list and delete metrics, and report stats about metrics

##Installation
To build from source, clone this repository and run 'gradlew shadowJar'. This will produce a metrics-VERSION-all.jar in the build/libs directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/appdynamics-metrics/releases).

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
    Thu Aug 24 00:00:00 CEST 2017;customer1;MyApp1;MyTier1;MyNode1;MACHINE_AGENT;43
    Thu Aug 24 00:00:00 CEST 2017;customer1;MyApp2;MyTier2;Mu=yNode2;APP_AGENT;66
    ...

Storage (default : mysql root@localhost:3388) 

    java -jar metrics-VERSION-all.jar -password=*** metrics-stats-storage
    
    Table;Count;SizeKB;
    "metricdata_hour";"3422";"7760";
    "metricdata_hour_agg";"3422";"7760";
    "metricdata_hour_agg_app";"3422";"7744";
    "metricdata_min";"0";"704";
    "metricdata_min_agg";"0";"176";
    "metricdata_min_agg_app";"0";"352";
    "metricdata_ten_min";"0";"1408";
    "metricdata_ten_min_agg";"0";"352";
    "metricdata_ten_min_agg_app";"0";"352";

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.
