package com.techmonad

import com.techmonad.domains.TrafficData
import org.apache.spark.sql._

object EngineApp {


  def main(args: Array[String]): Unit = {

    val dataFile = "modules/engine/src/main/resources/traffic-data.csv"

    val spark = buildSparkSession

    val fullDataFrame: DataFrame = loadDataFrame(spark, dataFile)
    val trafficDataset: Dataset[TrafficData] = toTrafficDataset(fullDataFrame)

    trafficDataset.printSchema()
    trafficDataset.show()

    val count = fullDataFrame.count()
    val partitionCount = Math.ceil(count / 500.0)

    trafficDataset
      //.repartition(partitionCount.toInt)
      .foreachPartition { itr: Iterator[TrafficData] =>
        itr.grouped(500).foreach { data =>
          sendData(data.toList)
          println("sending...................................")
        }
      }

    Thread.sleep(1000000)
    //spark.stop()
    println("Spark Engine")
  }

  def loadDataFrame: (SparkSession, String) => DataFrame = (spark, path) => {
    spark
      .read
      .format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
  }

  def buildSparkSession: SparkSession = {
    SparkSession
      .builder
      .appName("Engine Application")
      .master("local[*]")
      .getOrCreate()
  }

  def toTrafficDataset: DataFrame => Dataset[TrafficData] = dataFrame => {
    import dataFrame.sparkSession.implicits._
    val selectedDataFrame = dataFrame.select("id", "plateId", "in", "out")
    selectedDataFrame.as[TrafficData]
  }

  def sendData(list: List[TrafficData]) = {
    list.foreach { trafficData =>
      import sttp.client._
      import sttp.client.sprayJson._
      val request = basicRequest.post(uri"http://localhost:8080/traffic-details")
        .body(trafficData)
        .contentType("application/json")

      implicit val backend = HttpURLConnectionBackend()
      val response = request.send()

      // response.header(...): Option[String]
      //println(response.header("Content-Length"))

      // response.body: by default read into an Either[String, String] to indicate failure or success
     // println(response.body)
    }
  }


}
