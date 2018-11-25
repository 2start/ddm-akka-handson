import PipelineSupervisor.CrackedPasswords
import PwCrackService.HashRangeCheckRequest
import PwCracker.{PasswordCheckRequest, PasswordCheckResponse}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object PwCrackService {
  def props: Props = Props[PwCrackService]

  final case class HashRangeCheckRequest(hashes: Vector[String], workers: Vector[ActorRef])
}

class PwCrackService extends TaskService {
  override def receive: Receive = {
    case HashRangeCheckRequest(hashes, w)  =>
      workers = w
      distributeHashes(hashes, sender)
  }

  def distributeHashes(hashes: Vector[String], replyTo: ActorRef): Unit = {
    val aggregator = context.actorOf(Props(classOf[CrackedPasswordsAggregator], hashes.size, replyTo))
    val router = createRouter()

    for(hash <- hashes) {
      val start = 100000
      val stop = 999999

      router.route(PasswordCheckRequest(hash, start, stop), aggregator)
      log.debug(s"Distributed password cracking task $hash. Check $start to $stop")
    }
  }

}

class CrackedPasswordsAggregator(expectedPasswords: Int, replyTo: ActorRef) extends Actor with ActorLogging {
  var crackedPasswords = Map.empty[String, String]

  override def receive: Receive = {
    case PasswordCheckResponse(hash, start, stop, Some(password)) =>
      crackedPasswords = crackedPasswords + (hash -> password)
      log.debug(s"Found $hash:$password on worker $sender")
      if (expectedPasswords == crackedPasswords.size) replyTo ! CrackedPasswords(crackedPasswords)
  }
}
