import LinearCombinationFinder.LinearCombinationCheckRequest
import LinearCombinationService.LinearCombinationFound
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable

object LinearCombinationFinder {
  def props = Props(new LinearCombinationFinder)
  final case class LinearCombinationCheckRequest(values: Vector[Int], firstCoefficients: Vector[Int])
}

class LinearCombinationFinder extends Actor with ActorLogging {
  override def receive: Receive = {
    case LinearCombinationCheckRequest(values, firstCoefficients) =>
      findLinearCombination(values, firstCoefficients, sender)
  }

  def findLinearCombination(values: Vector[Int], firstCoefficients: Vector[Int], sender: ActorRef): Unit = {
    var target = 0
    if (firstCoefficients.nonEmpty) {
      target = firstCoefficients.zipWithIndex.map({ case (coefficient, index) => coefficient * values(index) }).sum
    }
    val result = new mutable.ListBuffer[Int]()
    if (findLinearCombinationRec(values, firstCoefficients.length, target, result)) {
      val coefficients = firstCoefficients ++ result.toVector
      sender ! LinearCombinationFound(coefficients)
      log.info(s"Linear combination for $values found: $coefficients")
    }
  }

  def findLinearCombinationRec(values: Vector[Int], i: Integer, target: Int, result: mutable.ListBuffer[Int]): Boolean = {
    if(i == values.length) {
      if (target == 0) {
        return true
      }
      return false
    }
    if(findLinearCombinationRec(values, i+1, target+values(i), result)) {
      1 +=: result
      return true
    }
    if(findLinearCombinationRec(values, i+1, target-values(i), result)) {
      -1 +=: result
      return true
    }
    false
  }
}
