import java.security.MessageDigest

import PwCracker.{PasswordCheckRequest, PasswordCheckResponse}
import akka.actor.{Actor, ActorLogging, Props}

object PwCracker {
  def props = Props(new PwCracker)

  final case class PasswordCheckRequest(hash: String, start: Int, stop: Int)
  final case class PasswordCheckResponse(hash: String, start: Int, stop: Int, password: Option[String])
}

class PwCracker extends Actor with ActorLogging {
  val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")

  override def receive: Receive = {
    case PasswordCheckRequest(hash, start, stop) =>
      sender ! PasswordCheckResponse(hash, start, stop, checkRange(hash, start, stop))
  }

  override def preStart() = {
    log.info("Created Pw cracker.")
  }

  def checkRange(hash: String, start: Int, stop: Int): Option[String] = {
    for (i <- start until stop) {
      if (hash == calculateHash(i.toString)) return Some(i.toString)
    }
    None
  }

  // ~420ms average 250k hashs
  def calculateHash(password: String): String = {
    val hashedBytes = sha256.digest(password.getBytes("UTF-8"))
    val stringBuffer = new StringBuffer()
    for (i <- 0 until hashedBytes.length) {
      stringBuffer.append(Integer.toString((hashedBytes(i) & 0xff) + 0x100, 16).substring(1))
    }
    stringBuffer.toString
  }
}
