package com.techmonal

//#user-registry-actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.techmonal.domains.{ActionPerformed, TrafficData, TrafficDetails}

object TrafficDataRegistry {

  // actor protocol
  sealed trait Command

  final case class GetTrafficDetails(replyTo: ActorRef[TrafficDetails]) extends Command

  final case class CreateTrafficData(trafficData: TrafficData, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class CreateTrafficDataSet(trafficDataSet: List[TrafficData], replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetTrafficData(id: Int, replyTo: ActorRef[GetTrafficDataResponse]) extends Command

  final case class DeleteTrafficData(id: Int, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetTrafficDataResponse(maybeTrafficData: Option[TrafficData])

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(trafficDataSet: Set[TrafficData]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetTrafficDetails(replyTo) =>
        replyTo ! TrafficDetails(trafficDataSet.toSeq)
        Behaviors.same
      case CreateTrafficData(trafficData, replyTo) =>
        replyTo ! ActionPerformed(s"TrafficData ${trafficData.id} created.")
        registry(trafficDataSet + trafficData)
      case CreateTrafficDataSet(trafficDataInBulk, replyTo) =>
        replyTo ! ActionPerformed(s"${trafficDataInBulk.size} : Number of TrafficData created.")
        registry(trafficDataSet ++ trafficDataInBulk.toSet)
      case GetTrafficData(id, replyTo) =>
        replyTo ! GetTrafficDataResponse(trafficDataSet.find(_.id == id))
        Behaviors.same
      case DeleteTrafficData(id, replyTo) =>
        replyTo ! ActionPerformed(s"TrafficData $id deleted.")
        registry(trafficDataSet.filterNot(_.id == id))
    }
}
