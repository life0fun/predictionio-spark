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

To read from Kafka inside stream job, createDirectStream and foreachRDD.

  import org.apache.spark.streaming.kafka._

 val directKafkaStream = KafkaUtils.createDirectStream[
     [key class], [value class], [key decoder class], [value decoder class] ](
     streamingContext, [map of Kafka parameters], [set of topics to consume])

    directKafkaStream.foreachRDD { rdd => 
     val offsetRanges = rdd.asInstanceOf[HasOffsetRanges]
     // offsetRanges.length = # of Kafka partitions being consumed
     
    rdd.mapPartitionsWithIndex { (i, iter) =>
        // set up some connection
 
        iter.foreach {
          // use the connection
        }
 
        // close the connection
 
        Iterator.empty
      }.foreach {
        // Without an action, the job won't get scheduled, so empty foreach to force it
        // This is a little awkward, but there is no foreachPartitionWithIndex method on rdds
        (_: Nothing) => ()
      }



The call to createDirectStream returns a stream of tuples formed from each Kafka message’s key and value. The exposed return type is InputDStream[(K, V)], where K and V in this case are both String.

To read from Kafka in a non-streaming Spark job, use KafkaUtils.createRDD:

    val sc = new SparkContext(new SparkConf)
 
    // hostname:port for Kafka brokers, not Zookeeper
    val kafkaParams = Map("metadata.broker.list" -> "localhost:9092,anotherhost:9092")
 
    val offsetRanges = Array(
      OffsetRange("sometopic", 0, 110, 220),
      OffsetRange("sometopic", 1, 100, 313),
      OffsetRange("anothertopic", 0, 456, 789)
    )
 
    val rdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder](
      sc, kafkaParams, offsetRanges)


The call to createRDD returns a single RDD of (key, value) tuples for each Kafka message in the specified batch of offset ranges. The exposed return type is RDD[(K, V)], the private implementation is KafkaRDD.

## 

