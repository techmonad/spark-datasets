package com.techmonal

import com.techmonal.domains.TrafficData
import org.apache.spark.sql._

object EngineApp {


  def main(args: Array[String]): Unit = {

    val dataFile = "modules/engine/src/main/resources/traffic-data.csv"

    val spark = buildSparkSession

    val fullDataFrame = loadDataFrame(spark, dataFile)
    val trafficDataset = toTrafficDataset(fullDataFrame)

    //val count = trafficDataset.count()
    //val partition = Math.ceil(count / 200.0).toInt

    //println("Number of partitions : " + partition)

    //val partitionDataSet = trafficDataset.repartition(partition)

    trafficDataset.foreachPartition { dataItr: Iterator[TrafficData] =>
      dataItr.grouped(500).foreach{ groupedItr =>
        println("Data count : " + groupedItr.size)
        sendData(groupedItr)
      }
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

  def sendData: Seq[TrafficData] => Unit = trafficData => {
    import sttp.client._
    import sttp.client.sprayJson._

    val request = basicRequest.post(uri"http://localhost:8080/traffic-details/bulk")
      .body(trafficData)
      .contentType("application/json")

    implicit val backend = HttpURLConnectionBackend()
    val response = request.send()

    // response.body: by default read into an Either[String, String] to indicate failure or success
    println(response.body)
  }
}
