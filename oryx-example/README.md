# Oryx 2 example

1. Install cdh on mac

  http://blog.cloudera.com/blog/2014/09/how-to-install-cdh-on-mac-osx-10-9-mavericks/
  
  http://www.cloudera.com/content/cloudera/en/documentation/core/latest/topics/cdh_vd_cdh_package_tarball.html

  We install cloudera cdh jars under dev/cloudera/hadoop or hbase.
  
  We config running HBase in distributed or standalone mode to avoid download zookeeper. set hbase-site.xml/hbase.cluster.distributed to true/false.
  If distriuted, set HBASE_MANAGES_ZK in hbase-env.sh to tell hbase it should manage its own zk.


  Setup PATH.

    export PATH=${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${ZK_HOME}/bin:${HBASE_HOME}/bin:${HIVE_HOME}/bin:${HCAT_HOME}/bin:${M2_HOME}/bin:${ANT_HOME}/bin:${PATH}


2. Ensure services

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

    yarn resourcemanager
      http://localhost:8088/cluster
    yarn scheduler
      http://localhost:8088/cluster/scheduler
    yarn nodemanager
      http://localhost:8042/node

    mapred history server
      mapred historyserver  OR mr-jobhistory-daemon.sh start/stop historyserver

    Test Vanilla YARN Application
      hadoop jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.6.0-cdh5.4.1.jar -appname DistributedShell -jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.6.0-cdh5.4.1.jar -shell_command "ps wwaxr -o pid,stat,%cpu,time,command | head -10" -num_containers 2 -master_memory 1024

    hbase
      start-hbase.sh
      stop-hbase.sh

      http://localhost:60010/master-status

      hbase shell
      create 'URL_HITS', {NAME=>'HOURLY'},{NAME=>'DAILY'},{NAME=>'YEARLY'}
      put 'URL_HITS', 'com.cloudera.blog.osx.localinstall', 'HOURLY:2014090110', '10'
      put 'URL_HITS', 'com.cloudera.blog.osx.localinstall', 'HOURLY:2014090111', '5'
       
      scan 'URL_HITS'


2. Run oryx
  1. update compute-classpath.sh to set correct hadoop and cdh jar path.

go to bin/ directory, Run the three Layers with:

    ./run.sh --layer-jar oryx-batch-2.0.0-SNAPSHOT.jar --conf example.conf

    ./run.sh --layer-jar oryx-speed-2.0.0-SNAPSHOT.jar --conf example.conf

    ./run.sh --layer-jar oryx-serving-2.0.0-SNAPSHOT.jar --conf example.conf