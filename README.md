cassandra-summit-demo
====

Example ETL and analytics workflow using Cassandra and Hadoop.

Flow:
----

1. Load unstructured data into HDFS
> hadoop dfs -put <local> <data>

2. Use Hadoop Streaming to add structure and insert into Cassandra
> bin/load -input <in>

3. Summarize data with Pig into HDFS
> TODO:

> bin/summarize -input <in> -output <out>

4. Use Java MapReduce to store results back into Cassandra

Dependencies:
----

FIXME: Load Cassandra JARs from Cassandra install path

* PyYAML
* Avro 1.3.3 Python
* Cloudera DH2: Hadoop Streaming, Apache Pig

Thank you to Infochimps for the carefully scraped Wikipedia dataset:
http://infochimps.org/datasets/taxobox-wikipedia-infoboxes-with-taxonomic-information-on-animal

