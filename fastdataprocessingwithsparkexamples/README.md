# fast data processing with spark examples

Examples for Fast Data Processing with Spark soon to be available from packt.
For the GeoIp example you will have to download and install the maxmind GeoIp database from http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz

sbt/sbt sbt-version

To build
	sbt compile

to package
	sbt package

to make a deployable jar run 
	sbt assembly

To assemble all dependency jars,
  sbt assemblyPackageDependency

All:
  sbt clean package assemblyPackageDependency


## sbt 

Note spark 1.2 only works with scala 2.9.3. Need to modify scala formulor to install scala 2.9.3.

1. To view dependency tree
	sbt dependency-tree

2. To enforce a version, use force().
  "org.slf4j" % "slf4j-log4j12" % "1.6.1" force(),

  Eviction rule:
    https://typesafe.com/blog/improved-dependency-management-with-sbt-0137
    https://github.com/sbt/sbt-native-packager/issues/291

3. sbt 'show discoveredMainClasses'
   sbt run <args>
   sbt "run-main com.alvinalexander.Foo"
   sbt "project foo" "run arg1 arg2"


4. To get rid of SecurityException: Invalid signature file digest for Manifest main attributes,
we need to exclude META-INF in the mergeStrategy in assembly.

  case PathList("META-INF", xs @ _*) => MergeStrategy.discard


## Run on Spark

We can run sbt package to create jar file and use spark-submit to run it.

1. brew install apache-spark

2. spark-submit with args.

  spark-submit --jars $(echo /dir/of/jars/*.jar | tr ' ' ',') \
    --class "SimpleApp" --master local[4] path/to/myApp.jar

  spark-submit --verbose \
  --jars target/scala-2.10/fastdataprocessingwithsparkexamples-assembly-0.1-SNAPSHOT-deps.jar \
  --master local[4] \
  --class pandaspark.examples.GeoIpExample target/scala-2.10/fastdataprocessingwithsparkexamples_2.10-0.1-SNAPSHOT.jar local[4] ip_delay.csv

