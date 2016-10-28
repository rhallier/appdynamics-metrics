# AppDynamics Metrics Tool

A simple tool to list and delete metrics

##Installation
To build from source, clone this repository and run 'mvn clean install'. This will produce a metrics-VERSION-jar-with-dependencies.jar in the target directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/appdynamics-metrics/releases).

##Usage

### Help 

    java -jar metrics-VERSION-jar-with-dependencies.jar
    Usage : [-hostname=] [-port=] [-username=] [-password=] applications|tiers|metrics list [filter]|metrics delete filter

### List metrics 

List all the metrics
    java -jar metrics-VERSION-jar-with-dependencies.jar -hostname=localhost -port=3388 -username=root -password=*** metrics list 

	...
	Deletable=true , app_id=4, id=1580, name=BTM|BTs|BT:29|Component:6|Average Request Size
	Deletable=true , app_id=4, id=1581, name=BTM|BTs|BT:29|Component:6|Exit Call:CUSTOM|To:{[UNRESOLVED][1]}|Average Response Time (ms)
	Deletable=true , app_id=4, id=1582, name=BTM|BTs|BT:29|Component:6|Exit Call:CUSTOM|To:{[UNRESOLVED][1]}|Calls per Minute
	Deletable=true , app_id=4, id=1583, name=BTM|BTs|BT:29|Component:6|Exit Call:CUSTOM|To:{[UNRESOLVED][1]}|Errors per Minute
	...

List all the metrics containing 'JMX'
    java -jar metrics-VERSION-jar-with-dependencies.jar -hostname=localhost -port=3388 -username=root -password=*** metrics list JMX
    
    ...
    Deletable=true , app_id=4, id=843, name=JMX|Performance Counters|Cache|Metric|Hour-cache-count
	Deletable=true , app_id=4, id=844, name=JMX|Performance Counters|Cache|Metric|Hour-cache-size (MB)
	Deletable=true , app_id=4, id=845, name=JMX|Performance Counters|Cache|Metric|Min-cache-count 
	...
	
### Delete metrics 

__Warning : Please do a db backup before, there is no rollback operation, so please double-check your delete query !!!!__ 

You must stop the controller (./controller.sh stop-appserver), not the db

Delete all the metrics containing '|ECommerce-Services|JMX|JDBC Connection Pools|'

    java -jar metrics-VERSION-jar-with-dependencies.jar -hostname=localhost -port=3388 -username=root -password=*** metrics delete "|ECommerce-Services|JMX|JDBC Connection Pools|"

Restart the controller (./controller.sh start-appserver)
    

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.
