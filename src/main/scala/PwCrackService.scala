import PwCracker.{PasswordCheckRequest, PasswordCheckResponse}
import PwCrackService.HashRangeCheckRequest
import PipelineSupervisor.CrackedPasswords
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

object PwCrackService {
  def props: Props = Props[PwCrackService]

  final case class HashRangeCheckRequest(hashes: Vector[String])
}

class PwCrackService extends Actor with ActorLogging {

  val pwCrackRouter: ActorRef = context.actorOf(FromConfig.props(Props[PwCracker]), "pwCrackRouter")
  var passwordCrackers = Vector.empty[ActorRef]

  override def receive: Receive = {
    case HashRangeCheckRequest(hashes)  =>
      distributeHashes(hashes, sender)
  }

  def distributeHashes(hashes: Vector[String], replyTo: ActorRef): Unit = {
    val aggregator = context.actorOf(Props(classOf[CrackedPasswordsAggregator], hashes.size, replyTo))

    for(hash <- hashes) {
      val start = 100000
      val stop = 999999

      pwCrackRouter.tell(PasswordCheckRequest(hash, start, stop), aggregator)
      log.info(s"Distributed password cracking task $hash. Check $start to $stop")
    }
  }

  override def preStart(): Unit = {
    log.info(s"Created password cracking service.")
  }
}

class CrackedPasswordsAggregator(expectedPasswords: Int, replyTo: ActorRef) extends Actor with ActorLogging {
  var crackedPasswords = Map.empty[String, String]

  override def receive: Receive = {
    case PasswordCheckResponse(hash, start, stop, Some(password)) =>
      crackedPasswords = crackedPasswords + (hash -> password)
      log.info(s"Found $hash:$password on worker $sender")
      if (expectedPasswords == crackedPasswords.size) replyTo ! CrackedPasswords(crackedPasswords)
  }
}
