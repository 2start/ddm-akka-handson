import LinearCombinator.LinearCombinationCheckRequest
import LinearCombinationService.LinearCombinationResponse
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable

object LinearCombinator {
  def props = Props(new LinearCombinator)
  final case class LinearCombinationCheckRequest(values: Vector[Int], firstCoefficients: Vector[Int])
}

class LinearCombinator extends Actor with ActorLogging {
  override def receive: Receive = {
    case LinearCombinationCheckRequest(values, prefix) =>
      handleLinearCombinationRequest(values, prefix, sender)
  }

//  def handleLinearCombinationRequest(values: Vector[Int], firstCoefficients: Vector[Int], sender: ActorRef): Unit = {
//    var target = 0
//    if (firstCoefficients.nonEmpty) {
//      target = firstCoefficients.zipWithIndex.map({ case (coefficient, index) => coefficient * values(index) }).sum
//    }
//    val result = new mutable.ListBuffer[Int]()
//    if (findLinearCombinationRec(values, firstCoefficients.length, target, result)) {
//      val coefficients = firstCoefficients ++ result.toVector
//      sender ! LinearCombinationResponse(coefficients)
//      log.info(s"Linear combination for $values found: $coefficients")
//    }
//  }
//
//  def findLinearCombinationRec(values: Vector[Int], i: Integer, target: Int, result: mutable.ListBuffer[Int]): Boolean = {
//    if(i == values.length) {
//      if (target == 0) {
//        return true
//      }
//      return false
//    }
//    if(findLinearCombinationRec(values, i+1, target+values(i), result)) {
//      1 +=: result
//      return true
//    }
//    if(findLinearCombinationRec(values, i+1, target-values(i), result)) {
//      -1 +=: result
//      return true
//    }
//    false
//  }

  def handleLinearCombinationRequest(values: Vector[Int], prefix: Vector[Int], replyTo: ActorRef): Unit = {
    val target = prefix.zip(values).map{case (value, coefficient) => value*coefficient}.sum
    val tailValues = values.drop(prefix.length)

    val plusAnzahl = tailValues.length / 2
    val minusAnzahl = tailValues.length - plusAnzahl

    val result = spreadSearch(tailValues, plusAnzahl, minusAnzahl, target)
    log.info(s"target: ${target}, ${prefix.head}, $tailValues, $result")

    result match {
      case Some(combination) => replyTo ! LinearCombinationResponse(prefix ++ combination)
      case None =>
    }
  }

  def spreadSearch(values: Vector[Int], plusAnzahl: Int, minusAnzahl: Int, target: Int): Option[List[Int]] = {
    if (plusAnzahl > values.length || minusAnzahl < 0) return None
    // just check in one direction because of the symmetric solutions
    val result = findRec(values, List.empty[Int], plusAnzahl, minusAnzahl, 0, target)

    if (result.isDefined) result
    else spreadSearch(values, plusAnzahl+1, minusAnzahl-1, target)
  }

  def findRec(values: Vector[Int], prefix: List[Int], plusAnzahl: Int, minusAnzahl: Int, depth: Int, target: Int): Option[List[Int]] = {
    if (depth == values.length) {
      if (target == 0) return Some(prefix.reverse)
      else return None
    }

    if (plusAnzahl < 0) return None
    if (minusAnzahl < 0) return None

    findRec(values, 1 +: prefix, plusAnzahl-1, minusAnzahl, depth+1, target+values(depth)).orElse(
      findRec(values, -1 +: prefix, plusAnzahl, minusAnzahl-1, depth+1, target-values(depth)))
  }


}
