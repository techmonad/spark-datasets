package com.techmonal

import com.techmonal.domains.TrafficData
import org.apache.spark.sql._

object EngineApp {


  def main(args: Array[String]): Unit = {

    val dataFile = "modules/engine/src/main/resources/traffic-data.csv"

    val spark = buildSparkSession

    val fullDataFrame = loadDataFrame(spark, dataFile)
    val trafficDataset = toTrafficDataset(fullDataFrame)

    trafficDataset.printSchema()
    trafficDataset.show()

    trafficDataset.foreach { data =>
      sendData(data)
      println(data)
    }

    spark.stop()
    println("Spark Engine")
  }

  def loadDataFrame: (SparkSession, String) => DataFrame = (spark, path) => {
    spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(path)
  }

  def buildSparkSession: SparkSession = {
    SparkSession.builder
      .appName("Engine Application")
      .master("local[2]")
      .getOrCreate()
  }

  def toTrafficDataset: DataFrame => Dataset[TrafficData] = dataFrame => {
    import dataFrame.sparkSession.implicits._
    val selectedDataFrame = dataFrame.select("id", "plateId", "in", "out")
    selectedDataFrame.as[TrafficData]
  }

  def sendData: TrafficData => Unit = trafficData => {
    import sttp.client._
    import sttp.client.sprayJson._

    val request = basicRequest.post(uri"http://localhost:8080/traffic-details")
      .body(trafficData)
      .contentType("application/json")

    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    // response.header(...): Option[String]
    println(response.header("Content-Length"))

    // response.body: by default read into an Either[String, String] to indicate failure or success
    println(response.body)
  }
}
