package com.techmonad

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success

object WebApp {

  /**
   * Start HTTP Server
   *
   * @param routes
   * @param system
   */
  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bindFlow(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    // Server Bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val trafficDataRegistryActor = context.spawn(TrafficDataRegistry(), "TrafficDataRegistryActor")
      context.watch(trafficDataRegistryActor)

      val routes = new TrafficDataRoutes(trafficDataRegistryActor)(context.system)
      startHttpServer(routes.trafficDataRoutes, context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "WebAkkaHttpServer")
  }
}
