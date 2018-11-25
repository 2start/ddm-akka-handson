import MasterSupervisor._
import PipelineSupervisor.PipelineStart
import akka.actor.{ActorRef, Props}

object MasterSupervisor {
  def props(numWorkers: Int, numSlaves: Int, inputPath: String) =
    Props(new MasterSupervisor(numWorkers, numSlaves, inputPath))

  final case class SlaveJoined(workers: Map[String, Vector[ActorRef]])

  final case class PipelineFinished()

}

class MasterSupervisor(numWorkers: Int, numSlaves: Int, inputPath: String) extends WorkerSupervisor(numWorkers) {
  var slaves = Vector.empty[ActorRef]
  checkComplete()

  override def receive: Receive = {
    case SlaveJoined(slaveWorkers) =>
      addSlave(slaveWorkers, sender)
    case PipelineFinished() =>
      for (slave <- slaves)
        slave ! PipelineFinished()
      context.system.terminate()
  }

  def addSlave(slaveWorkers: Map[String, Vector[ActorRef]], slave: ActorRef): Unit = {
    for ((job, jobWorkers) <- slaveWorkers) {
      workers += (job -> (workers(job) ++ jobWorkers))
    }
    slaves = slaves :+ slave
    checkComplete()
  }

  def checkComplete(): Unit = {
    if (slaves == null)
      return
    if (slaves.length == numSlaves)
      context.actorOf(Props[PipelineSupervisor], "pipelineSupervisor") ! PipelineStart(workers, inputPath)
  }
}
