import akka.actor.ActorSystem
import scala.io.StdIn

object Main extends App {
  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("handsonSystem")

    try {
      var supervisor = system.actorOf(PipelineSupervisor.props(), "handsonSystemSupervisor")
      supervisor ! PipelineSupervisor.PipelineStart
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }

}