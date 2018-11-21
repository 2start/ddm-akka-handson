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
  var partnerIds = Vector.empty[Int]
  var prefixes = Vector.empty[Int]
  var partnerIdToHash = Map.empty[Int, String]
  var index: Int = -1
  var hashesReported = false

  override def receive: Receive = {
    case HashMiningRequest(ids, prfxs)  =>
      reportTo = sender
      partnerIds = ids
      prefixes = prfxs
      startHashMining()
    case HashFound(partnerId, hash) =>
      storeHash(partnerId, hash)
  }

  def startHashMining(): Unit = {
    log.info("Hash Mining started")
    val numberOfWorkers = 6
    for (_ <- 0 to numberOfWorkers) {
      try {
        queueNextJob()
      } catch {
        case _: IllegalStateException => return
      }
    }
  }

  def storeHash(partnerId: Int, hash: String): Unit = {
    if (!partnerIdToHash.contains(partnerId)) {
      partnerIdToHash = partnerIdToHash + (partnerId -> hash)
    }
    try {
      queueNextJob()
    } catch {
      case e: IllegalStateException => reportMinedHashes()
    }
  }

  def nextIndex(): Int = {
    if (partnerIdToHash.size == partnerIds.length)
      throw new IllegalStateException("No unmined hashes left.")

    do {
      index += 1
      if (index >= partnerIds.length)
        index = 0
    } while (partnerIdToHash.contains(partnerIds(index)))
    index
  }

  def queueNextJob(): Unit = {
    val next = nextIndex()
    val prefix = if (prefixes(next) == -1) "00000" else "11111"
    hashMiningRouter.tell(HashMiner.HashMiningRequest(partnerIds(next), prefix), self)
    log.info(s"Hash Mining job for partnerId ${partnerIds(next)} queued")
  }

  def reportMinedHashes(): Unit = {
    if (hashesReported) return
    val hashVector = partnerIds.map(partnerId => partnerIdToHash(partnerId))
    reportTo ! MinedHashes(hashVector)
    hashesReported = true
    context.stop(hashMiningRouter)
    log.info("All hashes mined")

  }
}