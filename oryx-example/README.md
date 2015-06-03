# Oryx 2 example

## Build Oryx

    mvn -X -DskipTests --settings ~/.m2/default-settings.xml clean package

## Install cdh on mac

  http://blog.cloudera.com/blog/2014/09/how-to-install-cdh-on-mac-osx-10-9-mavericks/
  http://codesfusion.blogspot.com/2013/10/setup-hadoop-2x-220-on-ubuntu.html
  
  http://www.cloudera.com/content/cloudera/en/documentation/core/latest/topics/cdh_vd_cdh_package_tarball.html

  https://repository.cloudera.com/content/repositories/cdh-releases-rcs/org/apache/spark/spark-streaming-kafka-assembly_2.10/1.3.0-cdh5.4.1/

  We install cloudera cdh jars under 
    ~dev/cloudera/hadoop,hbase.
    ~/dev/cloudera/hadoop/etc/hadoop

  Download needed jars from repository.cloudera cdh-releases-rcs.

  We config running HBase in distributed or standalone mode to avoid download zookeeper. set hbase-site.xml/hbase.cluster.distributed to true/false.
  If distriuted, set HBASE_MANAGES_ZK in hbase-env.sh to tell hbase it should manage its own zk.


  Setup PATH in $HOME/.profile

    export PATH=${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${ZK_HOME}/bin:${HBASE_HOME}/bin:${HIVE_HOME}/bin:${HCAT_HOME}/bin:${M2_HOME}/bin:${ANT_HOME}/bin:${PATH}

  Ensure version and classpath set properly.

    http://blog.cloudera.com/blog/2011/01/how-to-include-third-party-libraries-in-your-map-reduce-job/

    use -libjars” command line option of the hadoop jar ... command. 
    The jar will be placed in distributed cache and will be available to all of the job’s task.
    ${mapred.local.dir}/taskTracker/archive/${user.name}/distcache/… subdirectories on local nodes

    job.addFileToClassPath(new Path("pathToJar")); 

    hadoop version
    hadoop classpath

  All sorts of env variables for classpath.
    exportHADOOP_HOME=/Users/hyan2/dev/cloudera/hadoop
    export HADOOP_CLASSPATH=”./:/usr/lib/hbase/hbase-0.94.6.1.3.0.0-107-security.jar:`hadoop classpath`”;
    CLASSPATH=$CLASSPATH:$HADOOP_HOME/lib/hadoop-lzo.jar
    export JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH:$HADOOP_HOME/lib/native/
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HADOOP_HOME/lib/native/


## Ensure services are running
  First, make sure `hadoop version` is Hadoop 2.6.0-cdh5.4.1

  ### hdfs
    <name>dfs.namenode.name.dir</name>
    <value>/Users/hyan2/dev/cloudera/ops/nn</value>
    <name>dfs.datanode.data.dir</name>
    <value>/Users/hyan2/dev/cloudera/ops/dn/</value>
    
    hdfs namenode -format
      common.Storage: Storage directory /Users/hyan2/dev/cloudera/ops/nn has been successfully formatted.
    
    hdfs namenode
      namenode.NameNode: NameNode RPC up at: localhost/127.0.0.1:8020
      http://localhost:50070/dfshealth.html#tab-overview

    hdfs datanode
    Test:
      hadoop fs -mkdir /tmp
      hadoop fs -put /tmp/vod.java /tmp/
      http://localhost:50075/browseDirectory.jsp?dir=%2F&nnaddr=127.0.0.1:8020

  ### yarn
    vi mapred-site.xml
      <name>mapreduce.framework.name</name>
      <value>yarn</value>
    
    yarn resourcemanager
      http://localhost:8088/cluster
    yarn scheduler
      http://localhost:8088/cluster/scheduler
    yarn nodemanager
      http://localhost:8042/node

    mapred history server
      mapred historyserver OR mr-jobhistory-daemon.sh start/stop historyserver

    Test Vanilla YARN Application
      hadoop jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.6.0-cdh5.4.1.jar -appname DistributedShell -jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.6.0-cdh5.4.1.jar -shell_command "ps wwaxr -o pid,stat,%cpu,time,command | head -10" -num_containers 2 -master_memory 1024

  ### start hadoop with 5 deamons, All must be presented.
      start-dfs.sh   // NameNode, DataNode, SecondaryNameNode
      start-yarn.sh  // ResourceManager, NodeManager

    /etc/hosts map 127.0.0.1 to hostname.
      127.0.0.1 l-sb872h2fd5-m.local
  
  hadoop jar mapreduce example.
      hadoop jar ./share/hadoop/mapreduce2/hadoop-mapreduce-examples-2.6.0-cdh5.4.1.jar pi 2 5
  yarn mapreduce example.
      yarn jar ./share/hadoop/mapreduce2/hadoop-mapreduce-examples-2.6.0-cdh5.4.1.jar pi 1 1

  ### hbase
      start-hbase.sh
      stop-hbase.sh

      http://localhost:60010/master-status

      hbase shell
      create 'URL_HITS', {NAME=>'HOURLY'},{NAME=>'DAILY'},{NAME=>'YEARLY'}
      put 'URL_HITS', 'com.cloudera.blog.osx.localinstall', 'HOURLY:2014090110', '10'
      put 'URL_HITS', 'com.cloudera.blog.osx.localinstall', 'HOURLY:2014090111', '5'
       
      scan 'URL_HITS'

## Start kafka and zookeeper

## Run oryx
  
1. create bin/ folder contains run.sh and compute-clsasspath.sh.
  set the correct hadoop and cdh jar path. Need to provide kafka jar with kafka_*.jar so compute-classpath.sh can grep it.
  add missing jars, zkclient, spark-streaming-kafka_*, etc.

2. create subdirectories for batch/serving/speed layer. Note can not have oryx- prefix as run.sh 

3. Create a configuration file for the app, specify host names, ports and directories. In particular, choose data and model directories on HDFS that exist and will be accessible to the user running Oryx binaries.

4. specify SPARK_CONF_DIR and point to where spark-env.sh.

5. modify oryx app conf file to reduce batch streaming executor mem and cores.
    executor-memory = "400m"
    executor-cores = 2
    num-executors = 2


6. Kafka setup
    ./oryx-run.sh kafka-setup

3. go to bin/ directory, Run the three Layers with:

  ./run.sh --layer-jar ../batch/target/oryx-batch-2.0.0-SNAPSHOT.jar --conf ../example.conf 

  ./run.sh --layer-jar oryx-speed-2.0.0-SNAPSHOT.jar --conf example.conf

  ./run.sh --layer-jar oryx-serving-2.0.0-SNAPSHOT.jar --conf example.conf