import HashMiner.HashMiningRequest
import HashMiningService.HashFound
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.util.Random

object HashMiner {
  def props = Props(new HashMiner)
  final case class HashMiningRequest(id: Int, value: Int, prefix: String)
}

class HashMiner extends Actor with ActorLogging with Hasher {
  val rand: Random = Random

  override def receive: Receive = {
    case HashMiningRequest(id, value, prefix) =>
      mineHash(id, value, prefix, sender)
  }

  def mineHash(id: Int, value: Int, prefix: String, sender: ActorRef): Unit = {
    while (true) {
      val nonce = rand.nextInt
      val hash = this.calculateHash((value + nonce).toString)
      if (hash.startsWith(prefix)) sender ! HashFound(id, hash)
    }
  }
}
