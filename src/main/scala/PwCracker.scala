import PwCracker.{PasswordCheckRequest, PasswordCheckResponse}
import akka.actor.{Actor, ActorLogging, Props}

object PwCracker {
  def props = Props(new PwCracker)

  final case class PasswordCheckRequest(hash: String, start: Int, stop: Int)
  final case class PasswordCheckResponse(hash: String, start: Int, stop: Int, password: Option[String])
}

class PwCracker extends Actor with ActorLogging with Hasher {
  override def receive: Receive = {
    case PasswordCheckRequest(hash, start, stop) =>
      sender ! PasswordCheckResponse(hash, start, stop, checkRange(hash, start, stop))
  }

  def checkRange(hash: String, start: Int, stop: Int): Option[String] = {
    for (i <- start until stop) {
      if (hash == calculateHash(i.toString)) return Some(i.toString)
    }
    None
  }
}
