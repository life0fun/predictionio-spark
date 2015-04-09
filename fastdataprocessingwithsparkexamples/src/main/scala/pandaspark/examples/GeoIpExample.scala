
package pandaspark.examples


import scala.math
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.SparkFiles
import org.apache.spark.util.Vector
import au.com.bytecode.opencsv.CSVReader
import java.util.Random
import java.io.StringReader
import java.io.File
import com.snowplowanalytics.maxmind.geoip.IpGeo

case class DataPoint(x: Vector, y: Double)

// multiple computations
// 1. map each country to binary feature to see countries covered by data through countries and bcast to all nodes.
// 2. mapWith for per partition latency estimate for each country.
// 3. for data that can not be serialize over the wire, create them in partition can cache them.
// 
object GeoIpExample {

  def main(args: Array[String]) {
    if (args.length != 2) {
      System.err.println("Usage: GeoIpExample <master> <inputfile>")
      System.exit(1)
    }
    val logger = LoggerFactory.getLogger("GeoIpExample")

    val master = args(0)   // local[n]: for a local mode, spark://[sparkip]: to point to a Spark cluster
    val inputFile = args(1)
    val iterations = 100
    val geoCityFile = "GeoLiteCity.dat"
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    // val sc = new SparkContext(master, "GeoIpExample", 
    //                           System.getenv("SPARK_HOME"),
    //                           Seq(System.getenv("JARS")))
  
    val invalidLineCounter = sc.accumulator(0)
    val ipDelays = sc.textFile(inputFile)  // geo mapping file passed in as
    // tuple2 (x.x.x.x,[D@6c350511) => 1.179.147.2 : [1385.0 248.997]
    // ipAddrs.foreach(a => {println(a._1 + " : " + a._2.mkString(" "))})
    val ipAddrs = ipDelays.flatMap(
      line => {
        try {
          val row = (new CSVReader(new StringReader(line))).readNext()
          Some((row(0), row.drop(1).map(_.toDouble)))  // a some tuple2 of (String, Array).
        }
        catch {
          case _ => {
            invalidLineCounter += 1
            None
          }
        }
      })

    // addFile sends file to all workers. worker uses SparkFiles.get(path:String) to access file.
    // GeoLiteCity.dat contains snowplowanalytics IpGeo rows.
    val geoFile = sc.addFile(geoCityFile)
    // ipCountries is sequence of (x.x.x.x code) each row.
    // mapWith fn takes addition param gened by (constructA: _ => IpGeo) (f: (T, A) => U)
    // constructA per partition pass to map (f: (T, A) => U)
    val ipCountries = ipAddrs.flatMapWith(_ => IpGeo(dbFile = SparkFiles.get(geoCityFile)))((pair, ipGeo) => {
        //getLocation gives back an option so we use flatMap to only output if it's a some type
        // ipAddr row tuple2 (1.179.147.2, [1385.0 248.997]), call to ipGeo(x.x.x.x) rets ipGeo object.
        ipGeo.getLocation(pair._1).map(
          c =>  // map ipAddr to ipGeo.countryCode
            (pair._1, c.countryCode)).toSeq  // toSequence of each row.
      })
    ipCountries.cache()  // (ipAddr._1, countryCode)

    // send computation partition to each node, and collect full knowledge back.
    // collect ipCountries segments from each partition and and form a complete countries map and bcast to all.
    val countries = ipCountries.values.distinct().collect()
    logger.info("countries values : " + countries.deep.mkString("\n"))  // a set of distinct countryCode
    val countriesBc = sc.broadcast(countries)

    // countriesSignal convert ipCountres (ipAddr, code) to (ipAddr, [0 0 1 0 0 ...])
    // for each ip country, map to a list of n, n is num of all countries.
    val countriesSignal = ipCountries.mapValues(
      countryCode =>
        countriesBc.value.map(  // transform countryCode => [0 1 0 0]
          s => if (countryCode == s) 1.
               else 0.
        )
    )
    // (ipAddr, Seq([delays], [0 0 1 0 0 ...])) => DataPoint([[delays+xx], zz])
    val dataPoints = ipAddrs.join(countriesSignal).map(
      input => {
        input._2 match {
          case (countryData, originalData) =>
            DataPoint(new Vector(countryData++originalData.slice(1,originalData.size-2)),
                      originalData(originalData.size-1))
        } 
      }
    )
    countriesSignal.cache()
    dataPoints.cache()
    
    val rand = new Random(53)
    var w = Vector(dataPoints.first.x.length, _ => rand.nextDouble)
    for (i <- 1 to iterations) {
      val gradient = dataPoints.map(
        p => (1 / (1 + math.exp(-p.y*(w dot p.x))) - 1) * p.y * p.x).reduce(_ + _)

      w -= gradient
    }
    println("Final w: "+w)
  }
}