import akka.actor.{ActorSystem, Props}

import scala.io.StdIn

object Main extends App {
  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("handsonSystem")

    try {
      val supervisor = system.actorOf(Props[PipelineSupervisor], "pipelineSupervisor")
      supervisor ! PipelineSupervisor.PipelineStart
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }

}