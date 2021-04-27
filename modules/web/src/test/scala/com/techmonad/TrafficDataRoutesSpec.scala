package com.techmonad

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import akka.actor.typed.scaladsl.adapter._
import com.techmonad.domains.TrafficData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TrafficDataRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real TrafficDataRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val trafficDataRegistry = testKit.spawn(TrafficDataRegistry())
  lazy val routes = new TrafficDataRoutes(trafficDataRegistry).trafficDataRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  "TrafficDataRoutes" should {
    "return no traffic-details if no present (GET /traffic-details)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/traffic-details")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"traffics":[]}""")
      }
    }

    "be able to add trafficData (POST /traffic-details)" in {
      val trafficData = TrafficData(1, "Kapi", "42", "jp")
      val userEntity = Marshal(trafficData).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/traffic-details").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"TrafficData 1 created."}""")
      }
    }

    "be able to remove traffic data (DELETE /traffic-details)" in {
      // traffic data the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/traffic-details/1")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"TrafficData 1 deleted."}""")
      }
    }
  }
}
