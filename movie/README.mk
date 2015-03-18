## PredictionIO Movie Recommendation

### Install

Single line installation, includes spark, hadoop, hbase and elasticsearch.

    $ bash -c "$(curl -s https://install.prediction.io/install.sh)"

### Config


1. conf/pio-env.sh
    
    SPARK_HOME=/home/abc/Downloads/spark-1.2.0-bin-hadoop2.4
    PIO_STORAGE_SOURCES_ELASTICSEARCH_TYPE=elasticsearch
    PIO_STORAGE_SOURCES_ELASTICSEARCH_HOSTS=localhost
    PIO_STORAGE_SOURCES_ELASTICSEARCH_PORTS=9300

1. hbase-site.xml

    <name>hbase.rootdir</name>
    <value>file:/Users/.../PredictionIO/vendors/hbase-0.98.6</value>
 
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/Users/.../PredictionIO/vendors/zookeeper</value>


3. Disable ipv6 to avoid NumberFormatException in DNS.
  go to "preference" -> "network" -> "wifi" -> "TCP/IP". and change the "configure IPv6" from auto to "manually".



## Start

PATH=$PATH:/home/yourname/predictionio/bin; export PATH

1. start elasticsearch 
   $PredictionIO/vendors/elasticsearch-1.4.2/bin/elasticsearch

2. start hbase
    $PredictionIO/vendors/hbase-0.98.6/bin/start-hbase.sh

3. start pio eventserver
    pio status
    pio eventserver

4. log file
    ~/dev/ml/PredictionIO/vendors/hbase-0.98.6/logs

## App

1. create app
    pio app list
    pio app new MyMovie

2. import event data 

    curl https://raw.githubusercontent.com/apache/spark/master/data/mllib/sample_movielens_data.txt --create-dirs -o data/sample_movielens_data.txt

    python data/import_eventserver.py --access_key Lzwt6a6Qdf11IGLG4YRPiWByJpvcNH1OxeGOt0ANLZ0eA3xlKrbwVudtp9eQctwl

3. As spark is in-memory, we need to first start-hbase, then pio train to spark-submit trained model, the finally deploy the built in-memory engine.

    pio build
    pio train

4. deploy will spark-submit io.prediction.workflow.CreateServer to spark cluster.

    pio deploy

    localhost:4040/ has spark job status.

    /Users/hyan2/dev/ml/PredictionIO/vendors/spark-1.2.0/bin/spark-submit 
    --class io.prediction.workflow.CreateServer 
    --name PredictionIO Engine Instance: AUtOKAoLwO9A99-ivX2L 
    --jars file:/Users/hyan2/.pio_store/engines/EoHylMhPZkBjYehhwkGfspWOuJNF97YJ/ccac29581687b9311211f217de8be0d44cb04b11/template-scala-parallel-recommendation_2.10-0.1-SNAPSHOT.jar,file:/Users/hyan2/.pio_store/engines/EoHylMhPZkBjYehhwkGfspWOuJNF97YJ/ccac29581687b9311211f217de8be0d44cb04b11/template-scala-parallel-recommendation-assembly-0.1-SNAPSHOT-deps.jar,/Users/hyan2/dev/ml/PredictionIO/lib/engines_2.10-0.8.5.jar,/Users/hyan2/dev/ml/PredictionIO/lib/engines-assembly-0.8.5-deps.jar 
    --files /Users/hyan2/dev/ml/PredictionIO/conf/hbase-site.xml 
    --driver-class-path /Users/hyan2/dev/ml/PredictionIO/conf /Users/hyan2/dev/ml/PredictionIO/lib/pio-assembly-0.8.5.jar 
    --engineInstanceId AUtOKAoLwO9A99-ivX2L 
    --ip localhost 
    --port 8000 
    --event-server-ip localhost 
    --event-server-port 7070


5. queries.json

    curl -H "Content-Type: application/json" -d '{ "user": "1", "num": 4 }' http://localhost:8000/queries.json

## update

To update the model periodically with new data, simply set up a cron job to call $pio train and $pio deploy. The engine will continue to serve prediction results during the re-train process. After the training is completed, $pio deploy will automatically shutdown the existing engine server and bring up a new process on the same port.
