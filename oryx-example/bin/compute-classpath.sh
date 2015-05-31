#!/usr/bin/env bash

# Copyright (c) 2014, Cloudera, Inc. All Rights Reserved.
#
# Cloudera, Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"). You may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for
# the specific language governing permissions and limitations under the
# License.

# CDH5.3+ specific classpath config

#HADOOP_CONF_DIR="/etc/hadoop/conf"
#CDH_JARS_DIR="/opt/cloudera/parcels/CDH/jars"
HADOOP_CONF_DIR="/Users/hyan2/dev/cloudera/hadoop/etc/hadoop/"
CDH_JARS_DIR="/Users/hyan2/dev/cloudera/jars/"

SPARK_CONF_DIR="/Users/hyan2/dev/cloudera/spark/etc/spark"

# Some are known to have multiple versions, and so are fully specified:

echo ${HADOOP_CONF_DIR}
echo ${SPARK_CONF_DIR}
ls -1 \
 ${CDH_JARS_DIR}/zookeeper-*.jar \
 ${CDH_JARS_DIR}/zkclient-*.jar \
 ${CDH_JARS_DIR}/spark-assembly-*.jar \
 ${CDH_JARS_DIR}/spark-streaming-kafka-assembly_*.jar \
 ${CDH_JARS_DIR}/hadoop-auth-*.jar \
 ${CDH_JARS_DIR}/hadoop-common-*.jar \
 ${CDH_JARS_DIR}/hadoop-hdfs-*.jar \
 ${CDH_JARS_DIR}/hadoop-mapreduce-client-core-*.jar \
 ${CDH_JARS_DIR}/hadoop-yarn-api-*.jar \
 ${CDH_JARS_DIR}/hadoop-yarn-client-*.jar \
 ${CDH_JARS_DIR}/hadoop-yarn-common-*.jar \
 ${CDH_JARS_DIR}/hadoop-yarn-server-web-proxy-*.jar \
 ${CDH_JARS_DIR}/hadoop-yarn-applications-distributedshell-*.jar \
 ${CDH_JARS_DIR}/commons-cli-1.2.jar \
 ${CDH_JARS_DIR}/commons-collections-*.jar \
 ${CDH_JARS_DIR}/commons-configuration-*.jar \
 ${CDH_JARS_DIR}/commons-lang-2.6.jar \
 ${CDH_JARS_DIR}/protobuf-java-*.jar \
 ${CDH_JARS_DIR}/kafka_*.jar \
 ${CDH_JARS_DIR}/snappy-java-*.jar \
 ${CDH_JARS_DIR}/htrace-core-*.jar \
 | grep -E "[0-9]\\.jar$"
