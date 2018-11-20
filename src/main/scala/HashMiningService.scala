import HashMiningService.{HashFound, HashMiningRequest}
import PipelineSupervisor.MinedHashes
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

object HashMiningService {
  final case class HashMiningRequest(partnerIds: Vector[Int], prefixes: Vector[Int])
  final case class HashFound(id: Int, hash: String)
}

class HashMiningService extends Actor with ActorLogging {
  val hashMiningRouter: ActorRef = context.actorOf(
    FromConfig.props(Props[HashMiner]),
    "hashMiningRouter"
  )

  var reportTo: ActorRef = _
  var partnerIds: Vector[Int] = _
  var prefixes: Vector[Int] = _
  var hashes = Map.empty[Int, String]
  var index = 0
  var minedIds = Set.empty[Int]

  override def receive: Receive = {
    case HashMiningRequest(partnerIds, prefixes)  =>
      this.reportTo = sender
      this.partnerIds = partnerIds
      startHashMining()
    case HashFound(id, hash) =>
      storeHash(id, hash)
  }

  def startHashMining(): Unit = {
    val numberOfWorkers = 6
    for (_ <- 0 to numberOfWorkers) {
      try {
        queueNextJob()
      } catch {
        case _: IllegalStateException => return
      }
    }
  }

  def storeHash(id: Int, hash: String): Unit = {
    if (!minedIds.contains(id)) {
      minedIds = minedIds + id
      hashes = hashes + (id -> hash)
    }
    try {
      queueNextJob()
    } catch {
      case e: IllegalStateException => reportMinedHashes()
    }
  }

  def nextIndex(): Int = {
    if (minedIds.size == partnerIds.length)
      throw new IllegalStateException

    while (minedIds.contains(index)) {
      index += 1
      if (index >= partnerIds.length)
        index = 0
    }
    index
  }

  def queueNextJob(): Unit = {
    val next = nextIndex()
    val prefix = if (prefixes(next) == -1) "00000" else "11111"
    hashMiningRouter.tell(HashMiner.HashMiningRequest(next, partnerIds(next), prefix), self)
  }

  def reportMinedHashes(): Unit = {
    val hashVector = partnerIds.zipWithIndex.map({ case (_, i) => hashes(i) })
    reportTo ! MinedHashes(hashVector)
    context.stop(hashMiningRouter)
  }
}