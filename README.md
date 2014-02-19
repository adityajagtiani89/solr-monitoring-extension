# AppDynamics Elasticsearch Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Solr is the popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.

##Installation

1. Run 'mvn clean install' from the solr-monitoring-extension directory
2. Download the file SolrMonitor.zip located in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In \<machineagent install dir\>/monitors/SolrMonitor/, open monitor.xml and configure the Solr parameters.
     <pre>
     &lt;argument name="host" is-required="true" default-value="localhost" /&gt;
     &lt;argument name="port" is-required="true" default-value="8983" /&gt;
     </pre>
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr


## Metrics

Core Metrics

| Metric Name 			| Description |
|-------------------------------|-------------|
|Number of Docs			| |
|Max Docs			| |


Query Statistics
The following metrics are reported under |QueryStats|

| Metric Name 			| Description |
|-------------------------------|-------------|
|Average Rate (requests per second)| 	|
|5 Minute Rate (requests per second)		| |
|15 Minute Rate (requests per second)		| |
|Average Timer Per Request (milliseconds)	| |
|Median Request Time (milliseconds)		| |
|95th Percentile Request Time (milliseconds)	||

Memory Statistics
The following metrics are reported under SystemMemoryStats

| Metric Name 			| Description |
|-------------------------------|-------------|
|Free Physical Memory Size (Bytes)		| 	|
|sizeTotal Physical Memory Size (Bytes)			| |
|Committed Virtual Memory Size (Bytes)		| |

Cache Statistics
The following metrics are reported under |Cache|

| Metric Name 			| Description |
|-------------------------------|-------------|
|queryResultCacheHitRatio| 	|
|queryResultCacheHitRatioCumulative		| |
|queryResultCacheSize		| |
|documentCacheHitRatio	| |
|documentCacheHitRatioCumulative		| |
|documentCacheSize	||
|fieldValueCacheHitRatio| 	|
|fieldValueCacheHitRatioCumulative		| |
|fieldValueCacheSize		| |
|filterCacheHitRatio	| |
|filterCacheHitRatioCumulative		| |
|filterCacheSize	||

## Custom Dashboard
![]()

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).

