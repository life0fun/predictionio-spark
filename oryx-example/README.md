# Oryx 2 example

## Build Oryx

    mvn -X -DskipTests --settings ~/.m2/default-settings.xml clean package

## Install cdh on mac

  http://blog.cloudera.com/blog/2014/09/how-to-install-cdh-on-mac-osx-10-9-mavericks/
  http://codesfusion.blogspot.com/2013/10/setup-hadoop-2x-220-on-ubuntu.html
  
  http://www.cloudera.com/content/cloudera/en/documentation/core/latest/topics/cdh_vd_cdh_package_tarball.html

  https://repository.cloudera.com/content/repositories/cdh-releases-rcs/org/apache/spark/spark-streaming-kafka-assembly_2.10/1.3.0-cdh5.4.1/

  We install cloudera cdh jars under 
    ~/dev/cloudera/hadoop,hbase.
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
 
    localhost:50070/
    http://localhost:8088/cluster

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
  
    brew install kafka
    export KAFKA_HOME="/Users/hyan2/dev/cloudera/kafka"
    export ZK_HOME="/Users/hyan2/dev/cloudera/zookeeper"
    export PATH=$PATH:${KAFKA_HOME}/bin

    First, start zk, then start kafka server
    $KAFKA_HOME/bin/zookeeper-server-start.sh config/zookeeper.properties
    $KAFKA_HOME/bin/kafka-server-start.sh config/server.properties
  
## Run oryx with oryx.conf
  
1. create bin/ folder contains oryx-run.sh and compute-clsasspath.sh.
  set the correct hadoop and cdh jar path. Need to provide kafka jar with kafka_*.jar so compute-classpath.sh can grep it.
  add missing jars, zkclient, spark-streaming-kafka_*, etc.

2. cp oryx-batch/serving/speed subdirectories.

3. oryx.conf app configuration
  a. choose data and model directories on HDFS that exist and will be accessible to the user running Oryx binaries.

  b. zk-servers = "localhost:2181"

  To verify zookeeper
  $ZK_HOME/bin/zkCli.sh
    ls /
    ls /zookeeper
  
4. specify SPARK_CONF_DIR and point to where spark-env.sh.

5. modify oryx app conf file to reduce batch streaming executor mem and cores.
    executor-memory = "400m"
    executor-cores = 2
    num-executors = 2

6. Oryx.conf streaming section configures spark streaming args that passes to yarn for any layer.

  streaming {
    executor-cores = 2
    num-executors = 2
    executor-memory = "400m"
  }

7. Under the hook, spark args are passed to spark.deploy.yarn.ExecutorLauncher to launch containers for spark jobs.

    org.apache.hadoop.yarn.server.resourcemanager.amlauncher.AMLauncher: 
    Command to launch container container_1434230823335_0007_01_000001
    {{JAVA_HOME}}/bin/java,-server,-Xmx512m,-Djava.io.tmpdir={{PWD}}/tmp,
    '-Dspark.executor.memory=1g',
    '-Dspark.app.name=OryxSpeedLayer',
    '-Dspark.yarn.dist.files=../oryx.conf',
    -Dspark.yarn.app.container.log.dir=<LOG_DIR>,
    org.apache.spark.deploy.yarn.ExecutorLauncher,--arg,'192.168.0.102:58641',
    --executor-memory,1024m,--executor-cores,2,
  

## kafka setup with oryx-run.sh

  We start kafka zookeeper first, then kafka broker.
    clientPort=2181  // zookeeper port
    broker.id=1
    port=9092        // the port socket server listen on
    
    $KAFKA_HOME/bin/zookeeper-server-start.sh config/zookeeper.properties
    $KAFKA_HOME/bin/kafka-server-start.sh config/server.properties

  kafka config info defined in oryx.conf.
    kafka-brokers = "localhost:9092"
    zk-servers = "localhost:2181/kafka

  ./oryx-run.sh kafka-setup --layer-jar ../oryx-batch/target/oryx-batch-2.0.0-SNAPSHOT.jar --conf ../oryx.conf

    Input   ZK      localhost:2181/kafka
            Kafka   localhost:9092
            topic   OryxInput
    Update  ZK      localhost:2181/kafka
            Kafka   localhost:9092
            topic   OryxUpdate

  Modify oryx-run.sh 
      kafka-topics.sh --create --zookeeper localhost:2181


7. go to bin/ directory, Run the three Layers with:

  ./oryx-run.sh batch --layer-jar ../oryx-batch/target/oryx-batch-2.0.0-SNAPSHOT.jar --conf ../oryx.conf
  
    DirectKafkaInputDStream:96 ArrayBuffer(org.apache.spark.SparkException: Couldn't find leader offsets for Set())

    KafkaUtils.createDirectKafkaDStream() must be called after msgs have been in kafka topic.


  ./oryx-run.sh serving --layer-jar ../oryx-serving/target/oryx-serving-2.0.0-SNAPSHOT.jar --conf ../oryx.conf
    http://localhost:8080/

  
  ./oryx-run.sh speed --layer-jar ../oryx-speed/target/oryx-speed-2.0.0-SNAPSHOT.jar --conf ../oryx.conf

    Name: OryxSpeedLayer
    Application Type: SPARK
      Uncaught exception: Invalid resource request, requested virtual cores < 0, or requested virtual cores > max configured, requestedVirtualCores=4, maxVirtualCores=2

## Deploy your customized App

  Copy the resulting JAR file – call it myapp.jar – to the directory containing the Oryx binary JAR file it will be run with.

  Change your Oryx .conf file to refer to your custom Batch, Speed or Serving implementation class, as appropriate.

  When running the Batch / Speed / Serving Layers, add 
    oryx-run.sh --app-jar myapp.jar

