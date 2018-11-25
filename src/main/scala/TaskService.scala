import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

abstract class TaskService extends Actor with ActorLogging {
  var workers = Vector.empty[ActorRef]

  def createRouter(): Router = {
    Router(RoundRobinRoutingLogic(), workers.map(actorRef => ActorRefRoutee(actorRef)))
  }

  def stopRouter(): Unit = {
    workers.foreach(w => context.stop(w))
  }
}
