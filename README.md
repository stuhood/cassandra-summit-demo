cassandra-summit-demo
====

Example ETL and analytics workflow using Cassandra and Hadoop.

Flow:
----

1. Load unstructured data into HDFS
> hadoop dfs -put <local> <data>

2. Use Hadoop Streaming to add structure and insert into Cassandra
> bin/load -input <in>

3. Analyze data with Pig into HDFS
> cat bin/analyze.pig | bin/analyze

4. Use Java MapReduce to store summary results back into Cassandra
> bin/summarize

Dependencies:
----

* Cassandra 0.7.0-SNAPSHOT
* PyYAML
* Avro 1.3.3 Python
* Cloudera DH2: Hadoop Streaming, Apache Pig

Thank you to Infochimps for the carefully scraped Wikipedia dataset:
http://infochimps.org/datasets/taxobox-wikipedia-infoboxes-with-taxonomic-information-on-animal

