# Spark Stream with Kafka

We can configure Spark Streaming to receive data from Kafka directly.

To read from kafka from a Spark Streaming job, use KafkaUtils.createDirectStream.


## Kafka

    git clone 
    rm -fr /tmp/zookeeper* && rm -fr /tmp/kafka-log*

    bin/zookeeper-server-start.sh config/zookeeper.properties
    bin/kafka-server-start.sh config/server.properties

    bin/kafka-topics.sh --zookeeper localhost:2181     --create --topic test --partitions 1 --replication-factor 1

    ./bin/kafka-console-producer.sh --broker-list 192.168.0.102:9092 --topic test

    ./bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning

## Create Direct Stream

  import org.apache.spark.streaming.kafka._

 val directKafkaStream = KafkaUtils.createDirectStream[
     [key class], [value class], [key decoder class], [value decoder class] ](
     streamingContext, [map of Kafka parameters], [set of topics to consume])

directKafkaStream.foreachRDD { rdd => 
     val offsetRanges = rdd.asInstanceOf[HasOffsetRanges]
     // offsetRanges.length = # of Kafka partitions being consumed
     ...
 }


The call to createDirectStream returns a stream of tuples formed from each Kafka message’s key and value. The exposed return type is InputDStream[(K, V)], where K and V in this case are both String. 
## 

