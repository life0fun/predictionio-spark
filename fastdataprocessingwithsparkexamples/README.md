# fastdataprocessingwithsparkexamples

Examples for Fast Data Processing with Spark soon to be available from packt.
For the GeoIp example you will have to download and install the maxmind GeoIp database from http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz

sbt/sbt sbt-version

To build run 
	sbt/sbt compile and 

to package run 
	sbt/sbt package or 

to make a deployable jar run 
	sbt/sbt assembly

## sbt 

1. To view dependency tree
	sbt dependency-tree

2. To enforce a version, use force().
  "org.slf4j" % "slf4j-log4j12" % "1.6.1" force(),

2. https://github.com/sbt/sbt-native-packager/issues/291
