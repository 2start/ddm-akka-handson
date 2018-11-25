import LinearCombinationService.{LinearCombinationRequest, LinearCombinationResponse}
import LinearCombinator.LinearCombinationCheckRequest
import PipelineSupervisor.LinearCombination
import akka.actor.ActorRef

import scala.collection.mutable

object LinearCombinationService {
  final case class LinearCombinationRequest(passwords: Vector[String], workers: Vector[ActorRef])
  final case class LinearCombinationResponse(coefficients: Vector[Int])
}

class LinearCombinationService extends TaskService {
  var reportTo: ActorRef = _
  var reported: Boolean = false

  override def receive: Receive = {
    case LinearCombinationRequest(passwords, w)  =>
      workers = w
      this.reportTo = sender
      distributeLinearCombinations(passwords)
    case LinearCombinationResponse(coefficients) =>
      finalizeLinearCombinationSearch(coefficients)
  }

  def distributeLinearCombinations(passwords: Vector[String]): Unit = {
    val router = createRouter()
    log.info("Starting Linear Combination workers")
    val passwordsInt = passwords.map(password => password.toInt)
    val numWorkers = workers.length
    for (vector <- createBinaryVectors((Math.log(numWorkers)/Math.log(2)).ceil.toInt)) {
      router.route(LinearCombinationCheckRequest(passwordsInt, vector), self)
      log.info(s"Start worker with prefix $vector")
    }
    log.info("All Linear Combination workers started")
  }

  def finalizeLinearCombinationSearch(coefficients: Vector[Int]): Unit = {
    if (!reported) {
      reportTo ! LinearCombination(coefficients)
      reported = true
      stopRouter()
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
