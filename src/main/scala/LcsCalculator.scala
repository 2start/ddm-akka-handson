import LcsCalculator.{LcsRequest, LcsResponse}
import akka.actor.{Actor, ActorLogging}

object LcsCalculator {
  case class LcsRequest(string1: String, string2: String)
  case class LcsResponse(length: Int)
}

class LcsCalculator extends Actor with ActorLogging{
  override def receive: Receive = {
    case LcsRequest(string1, string2) =>
      sender ! LcsResponse(lcsLength(string1, string2))
  }

  def lcsLength(string1: String, string2: String): Int = {
    val length1 = string1.length
    val length2 = string2.length
    val chars1 = string1.toCharArray
    val chars2 = string2.toCharArray


    val lcs = Array.fill[Int](length1 + 1,length2 + 1)(0)
    var maxSubstringLength = 0

    for (i <- 1 to length1) {
      for (j <- 1 to length2) {
        if (chars1(i-1) == chars2(j-1)) {
          lcs(i)(j) = 1 + lcs(i-1)(j-1)
          if (lcs(i)(j) > maxSubstringLength) maxSubstringLength = lcs(i)(j)
        }
      }
    }

    maxSubstringLength
  }
}
