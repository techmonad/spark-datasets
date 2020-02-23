package com.techmonad.domains

import spray.json.DefaultJsonProtocol

final case class TrafficData(id: Int, plateId: String, in: String, out: String)

final case class TrafficDetails(traffics: Seq[TrafficData])

final case class ActionPerformed(description: String)

object TrafficData extends DefaultJsonProtocol {
  implicit val trafficDataFormat = jsonFormat4(TrafficData.apply)
}