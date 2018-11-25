import HashMiningService.{HashFound, HashMiningRequest}
import PipelineSupervisor.MinedHashes
import akka.actor.ActorRef
import akka.routing.Router

object HashMiningService {
  final case class HashMiningRequest(partnerIds: Vector[Int], prefixes: Vector[Int], workers: Vector[ActorRef])
  final case class HashFound(id: Int, hash: String)
}

class HashMiningService extends TaskService {
  var reportTo: ActorRef = _
  var partnerIds = Vector.empty[Int]
  var prefixes = Vector.empty[Int]
  var partnerIdToHash = Map.empty[Int, String]
  var index: Int = -1
  var hashesReported = false
  var router: Option[Router] = None

  override def receive: Receive = {
    case HashMiningRequest(ids, prfxs, w)  =>
      workers = w
      reportTo = sender
      partnerIds = ids
      prefixes = prfxs
      startHashMining()
    case HashFound(partnerId, hash) =>
      storeHash(partnerId, hash)
  }

  def startHashMining(): Unit = {
    router = Some(createRouter())
    val numWorkers = workers.length
    for (_ <- 0 to numWorkers) {
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
    router.foreach(r => r.route(HashMiner.HashMiningRequest(partnerIds(next), prefix), self))
    log.debug(s"Hash Mining job for partnerId ${partnerIds(next)} queued")
  }

  def reportMinedHashes(): Unit = {
    if (hashesReported) return
    val hashVector = partnerIds.map(partnerId => partnerIdToHash(partnerId))
    reportTo ! MinedHashes(hashVector)
    hashesReported = true
    stopRouter()
    log.debug("All hashes mined")

  }
}