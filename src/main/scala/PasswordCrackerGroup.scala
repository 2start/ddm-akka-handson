import PasswordCracker.{PasswordCheckRequest, PasswordCheckResponse}
import PasswordCrackerGroup.{HashRangeCheckRequest, HashRangeCheckResponse}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object PasswordCrackerGroup {
  def props(requestor: ActorRef): Props = Props(new PasswordCrackerGroup(requestor))

  final case class HashRangeCheckRequest(hashes: Vector[String])
  final case class HashRangeCheckResponse(crackedHashes: Map[String, String])
}

class PasswordCrackerGroup(requestor: ActorRef) extends Actor with ActorLogging {
  var hashes = Vector.empty[String]
  var crackedHashes = Map.empty[String, String]
  var passwordCrackers = Vector.empty[ActorRef]

  override def receive: Receive = {
    case HashRangeCheckRequest(hashes)  =>
      distributeHashes(hashes)
      this.hashes = hashes
    case PasswordCheckResponse(hash: String, start, stop, Some(password: String)) =>
      crackedHashes = crackedHashes + (hash -> password)
      log.info(s"${self}: Found ${hash}:${password} on worker ${sender}")
      if (hashes.size == crackedHashes.size) requestor ! HashRangeCheckResponse(crackedHashes)
  }

  def distributeHashes(hashes: Vector[String]): Unit = {
    for(hash <- hashes) {
      val passwordCracker = context.actorOf(PasswordCracker.props)
      passwordCrackers = passwordCrackers :+ passwordCracker
      val start = 111111
      val stop = 999999
      passwordCracker ! PasswordCheckRequest(hash, start, stop)
      log.info(s"Creating actor ${passwordCracker} for ${hash}. Check ${start} to ${stop}")
    }
  }
}
