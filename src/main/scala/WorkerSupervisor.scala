import akka.actor.{Actor, ActorLogging, ActorRef, Props}

abstract class WorkerSupervisor(numWorkers: Int) extends Actor with ActorLogging {
  var workers: Map[String, Vector[ActorRef]] = createLocalWorkers(numWorkers)

  checkComplete()

  def checkComplete()

  def createLocalWorkers(n: Int): Map[String, Vector[ActorRef]] = {
    Map(
      "pwCrackService" -> (1 to n).map(x => context.actorOf(Props[PwCracker])).toVector,
      "geneAnalysisService" -> (1 to n).map(x => context.actorOf(Props[LcsCalculator])).toVector,
      "linearCombinationService" -> (1 to n).map(x => context.actorOf(Props[LinearCombinator])).toVector,
      "hashMiningService" -> (1 to n).map(x => context.actorOf(Props[HashMiner])).toVector
    )
  }
}
