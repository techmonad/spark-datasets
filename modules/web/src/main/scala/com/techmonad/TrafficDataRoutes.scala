package com.techmonad

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.techmonad.TrafficDataRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.techmonad.domains.{ActionPerformed, TrafficData, TrafficDetails}

class TrafficDataRoutes(trafficDataRegistry: ActorRef[TrafficDataRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.techmonad.util.JsonFormats._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getTrafficDetails(): Future[TrafficDetails] =
    trafficDataRegistry.ask(GetTrafficDetails)

  def getTrafficData(id: Int): Future[GetTrafficDataResponse] =
    trafficDataRegistry.ask(GetTrafficData(id, _))

  def createTrafficData(trafficData: TrafficData): Future[ActionPerformed] =
    trafficDataRegistry.ask(CreateTrafficData(trafficData, _))

  def createTrafficDataSet(trafficDataSet: List[TrafficData]): Future[ActionPerformed] =
    trafficDataRegistry.ask(CreateTrafficDataSet(trafficDataSet, _))

  def deleteTrafficData(id: Int): Future[ActionPerformed] =
    trafficDataRegistry.ask(DeleteTrafficData(id, _))

  val trafficDataRoutes: Route =
    pathPrefix("traffic-details") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getTrafficDetails())
            },
            post {
              entity(as[TrafficData]) { trafficData =>
                onSuccess(createTrafficData(trafficData)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path("bulk") {
          post {
            entity(as[List[TrafficData]]) { trafficDataSet =>
              onSuccess(createTrafficDataSet(trafficDataSet)) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }
          }
        },
        path(Segment) { id =>
          concat(
            get {
              rejectEmptyResponse {
                onSuccess(getTrafficData(id.toInt)) { response =>
                  complete(response.maybeTrafficData)
                }
              }
            },
            delete {
              onSuccess(deleteTrafficData(id.toInt)) { performed =>
                complete((StatusCodes.OK, performed))
              }
            })
        }
      )
    }
}
