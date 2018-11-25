import MasterSupervisor.SlaveJoined
import akka.actor.{ActorSelection, Props}

object SlaveSupervisor {
  def props(numWorkers: Int, masterSupervisor: ActorSelection) = Props(new SlaveSupervisor(numWorkers, masterSupervisor))
}

class SlaveSupervisor(numWorkers: Int, masterSupervisor: ActorSelection) extends WorkerSupervisor(numWorkers) {
  import MasterSupervisor.PipelineFinished
  override def receive: Receive = {
    case PipelineFinished() =>
      context.system.terminate()
  }

  def checkComplete(): Unit = {
    masterSupervisor ! SlaveJoined(workers)
  }
}
