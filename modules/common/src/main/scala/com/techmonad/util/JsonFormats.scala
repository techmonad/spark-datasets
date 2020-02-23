package com.techmonad.util

import com.techmonad.domains.{ActionPerformed, TrafficDetails}
import spray.json.DefaultJsonProtocol

object JsonFormats {

  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val usersJsonFormat = jsonFormat1(TrafficDetails)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
