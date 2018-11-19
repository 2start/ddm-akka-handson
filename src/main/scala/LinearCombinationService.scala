import LinearCombinationFinder.LinearCombinationCheckRequest
import LinearCombinationService.{LinearCombinationFound, LinearCombinationRequest}
import PipelineSupervisor.LinearCombination
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig

import scala.collection.mutable

object LinearCombinationService {
  final case class LinearCombinationRequest(passwords: Vector[String])
  final case class LinearCombinationFound(coefficients: Vector[Int])
}

class LinearCombinationService extends Actor with ActorLogging {
  val linearCombinationRouter: ActorRef = context.actorOf(
    FromConfig.props(Props[LinearCombinationFinder]),
    "linearCombinationRouter"
  )

  var reportTo: ActorRef = _
  var reported: Boolean = false

  override def receive: Receive = {
    case LinearCombinationRequest(passwords)  =>
      this.reportTo = sender
      distributeLinearCombinations(passwords)
    case LinearCombinationFound(coefficients) =>
      finalizeLinearCombinationSearch(coefficients)
  }

  def distributeLinearCombinations(passwords: Vector[String]): Unit = {
    log.info("Starting Linear Combination workers")
    val passwordsInt = passwords.map(password => password.toInt)
    for (vector <- createBinaryVectors(4)) {
      linearCombinationRouter.tell(LinearCombinationCheckRequest(passwordsInt, vector), self)
      log.info(s"Start worker with prefix $vector")
    }
    log.info("All Linear Combination workers started")
  }

  def finalizeLinearCombinationSearch(coefficients: Vector[Int]): Unit = {
    if (!reported) {
      reportTo ! LinearCombination(coefficients)
      reported = true
      context.stop(linearCombinationRouter)
      log.info("Stopped LinearCombinationService!")
    }
  }

  def createBinaryVectors(length: Int): Vector[Vector[Int]] = {
    val result = new mutable.ListBuffer[Vector[Int]]()
    val numberOfVectors = Math.pow(2, length).toInt
    for (i <- 0 until numberOfVectors) {
      result.append(
        toBinary(i, length).map(
          {case '0' => -1 case '1' => 1}
        ).toVector
      )
    }
    result.toVector
  }

  def toBinary(i: Int, digits: Int) : String =
    String.format("%" + digits + "s", i.toBinaryString).replace(' ', '0')
}
