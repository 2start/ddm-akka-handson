import GeneAnalysisService.{GenesWithId, IdToPartnerIdWithLength}
import LcsCalculator.{LcsRequest, LcsResponse}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

object GeneAnalysisService {
  // ids are expected to be numbered from 1 to genesWithId.length
  case class GenesWithId(genesWithId: Vector[(String, Int)])
  case class IdToPartnerIdWithLength(mappings: Map[Int, (Int, Int)])
}

class GeneAnalysisService extends Actor with ActorLogging{

  val lcsRouter: ActorRef = context.actorOf(FromConfig.props(Props[LcsCalculator]), "lcsRouter")

  override def receive: Receive = {
    case GenesWithId(genesWithId) =>
      distributeWork(genesWithId, sender)
  }

  def distributeWork(genesWithId: Vector[(String, Int)], replyTo: ActorRef): Unit = {
    val aggregator = context.actorOf(Props(classOf[BestPartnerAggregator], genesWithId.length, replyTo))

    for ((gene1, id1) <- genesWithId) {
      for ((gene2, id2) <- genesWithId) {
        if (id1 < id2) {
          lcsRouter.tell(LcsRequest(id1, id2, gene1, gene2), aggregator)
        }
      }
    }
  }
}

class BestPartnerAggregator(numberOfGenes: Int, replyTo: ActorRef) extends Actor with ActorLogging {
  var idToPartnerIdWithLength: Map[Int, (Int, Int)] = Map.empty
  var responseCount: Int = 0
  val expectedResponses: Int = (numberOfGenes * numberOfGenes - numberOfGenes) / 2

  override def receive: Receive = {
    case LcsResponse(id1, id2, length) =>
      log.info(s"Lcs reponse from ${sender}: ${id1}, ${id2}, length: ${length}")

      val id1PartnerLength = idToPartnerIdWithLength.getOrElse(id1, (0, 0))._2
      val id2PartnerLength = idToPartnerIdWithLength.getOrElse(id2, (0, 0))._2

      if (length >= id1PartnerLength) {
        idToPartnerIdWithLength += (id1 -> (id2, length))
      }

      if (length >= id2PartnerLength) {
        idToPartnerIdWithLength += (id2 -> (id1, length))
      }

      responseCount += 1
      if (responseCount == expectedResponses) {
        replyTo ! IdToPartnerIdWithLength(idToPartnerIdWithLength)
      }
  }
}
