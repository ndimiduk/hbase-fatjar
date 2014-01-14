# hbase fatjar

Demonstrate a Hadoop/HBase application built as a "fat jar". That is, an
application jar which includes its dependencies.

See https://issues.apache.org/jira/browse/HBASE-10304.

## packaging

Create the jars with

    $ mvn clean package

This produces to jars, fatjar-<VERSION>-job.jar and fatjar-<VERSION>.jar. The
former is the "fat jar" described earlier, the latter contains only the
application code.

## usage

Run the load data app `-w|--write` and the read data mapreduce job with by
omitting `-w|--write`. Optionally specify the table to use with `-t|--table`
(default: 'TestTable') and change the number of rows written with
`-n|--numRows` (default: 1000).

## invocation

The following permutations trigger the reported bug.

```
$ hadoop jar fatjar-1.0-SNAPSHOT-job.jar ...
$ HADOOP_CLASSPATH=/etc/hbase/conf hadoop jar fatjar-1.0-SNAPSHOT-job.jar ...
```

The following permutations correctly launch the application.

```
$ HADOOP_CLASSPATH=/path/to/hbase-protocol.jar:/etc/hbase/conf hadoop jar fatjar-1.0-SNAPSHOT-job.jar ...
$ HADOOP_CLASSPATH=$(hbase mapredcp):/etc/hbase/conf hadoop jar fatjar-1.0-SNAPSHOT-job.jar ...
$ HADOOP_CLASSPATH=$(hbase classpath) hadoop jar fatjar-1.0-SNAPSHOT-job.jar ...
```

And to be very pedantic about it, this also works.

```
$ HADOOP_CLASSPATH=$(hbase mapredcp):/etc/hbase/conf hadoop jar fatjar-1.0-SNAPSHOT.jar hbase_it.App -libjars $(hbase mapredcp | tr ':' ',') ...
```
