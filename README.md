cassandra-summit-demo
========

Example ETL and analytics workflow using Cassandra and Hadoop.

Flow:
1. Load unstructured data into HDFS
> hadoop dfs -put <local> <data>

2. Use Hadoop Streaming to add structure and insert into Cassandra
> bin/load -input <data>

3. Summarize data with Pig back into HDFS
4. Use Java MapReduce to store results in Cassandra


Dependencies:
# FIXME: Load Cassandra JARs from Cassandra install path
* PyYAML
* Avro 1.3.3 Python
* Cloudera DH2
** Apache Hadoop Streaming
** Apache Pig

Thank you to Infochimps for the carefully scraped Wikipedia dataset:
http://infochimps.org/datasets/taxobox-wikipedia-infoboxes-with-taxonomic-information-on-animal

