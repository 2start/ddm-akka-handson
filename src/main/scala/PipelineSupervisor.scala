import akka.actor.{Actor, ActorLogging, Props}

/**
  * A top level supervisor as recommended in https://doc.akka.io/docs/akka/current/guide/tutorial_2.html.
  */
class PipelineSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("Application started")
  override def postStop(): Unit = log.info("Application stopped")

  override def receive = Actor.emptyBehavior
}

object PipelineSupervisor {
  def props(): Props = Props(new PipelineSupervisor)
}
