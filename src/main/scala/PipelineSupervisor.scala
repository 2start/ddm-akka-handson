import GeneAnalysisService.{GenesWithId, IdToPartnerIdWithLength}
import HashMiningService.HashMiningRequest
import LinearCombinationService.LinearCombinationRequest
import MasterSupervisor.PipelineFinished
import PipelineSupervisor._
import PwCrackService.HashRangeCheckRequest
import Reader.{RawStudent, StudentsPath}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object PipelineSupervisor  {
  val props  = Props(new PipelineSupervisor)

  final case class PipelineStart(workers: Map[String, Vector[ActorRef]], inputPath: String)
  final case class StudentsData(students: Vector[RawStudent])
  final case class CrackedPasswords(crackedHashes: Map[String, String])
  final case class LinearCombination(coefficients: Vector[Int])
  final case class MinedHashes(hashes: Vector[String])
}

class PipelineSupervisor extends Actor with ActorLogging {
  var students = Vector.empty[RawStudent]
  var passwords = Vector.empty[String]
  var partners = Vector.empty[Int]
  var prefixes = Vector.empty[Int]
  var minedHashes = Vector.empty[String]
  var workers = Map.empty[String, Vector[ActorRef]]

  var hashMiningStarted = false
  var master: Option[ActorRef] = None

  override def receive: Receive = {
    case PipelineStart(jobsToWorkers, inputPath) =>
      workers = jobsToWorkers
      master = Some(sender)
      startPipeline(inputPath)
    case StudentsData(data) =>
      students = data
      startPwCrackService()
      startGeneAnalysisService()
    case CrackedPasswords(hashToPassword) =>
      passwords = students.map(student => hashToPassword(student.passwordHash))
      startLinearCombinationService()
    case IdToPartnerIdWithLength(studentIdToPartnerId) =>
      partners = students.map(student => studentIdToPartnerId(student.id)._1)
      startHashMiningService()
    case LinearCombination(coefficients) =>
      prefixes = coefficients
      startHashMiningService()
    case MinedHashes(hashes) =>
      minedHashes = hashes
      reportResults()
  }

  def startPipeline(inputPath: String): Unit = {
    val reader = context.actorOf(Reader.props())
    reader ! StudentsPath(inputPath)
  }

  def startPwCrackService(): Unit = {
    val hashes = students.map(student => student.passwordHash)
    val pwCrackService = context.actorOf(Props[PwCrackService], "pwCrackService")
    pwCrackService ! HashRangeCheckRequest(hashes, workers("pwCrackService"))
  }

  def startGeneAnalysisService(): Unit = {
    val genesWithId = students.map(student => (student.gene, student.id))
    val geneAnalysisService = context.actorOf(Props[GeneAnalysisService], "geneAnalysisService")
    geneAnalysisService ! GenesWithId(genesWithId, workers("geneAnalysisService"))
  }

  def startLinearCombinationService(): Unit = {
    val linearCombinationService = context.actorOf(Props[LinearCombinationService], "linearCombinationService")
    linearCombinationService ! LinearCombinationRequest(passwords, workers("linearCombinationService"))
  }

  def startHashMiningService(): Unit = {
    if (hashMiningStarted || prefixes.isEmpty || partners.isEmpty) return

    val hashMiningService = context.actorOf(Props[HashMiningService], name = "hashMiningService")
    hashMiningService ! HashMiningRequest(partners, prefixes, workers("hashMiningService"))
    hashMiningStarted = true
  }

  def reportResults(): Unit = {
    val prefixSymbols = prefixes.map({ case 1 => '+' case -1 => '-' })
    for ((RawStudent(id, name, _, _), i) <- students.zipWithIndex) {
      log.info(
        s"Student '$name' (id=$id): " +
          s"Password = '${passwords(i)}', " +
          s"Partner = '${partners(i)}', " +
          s"Prefix = '${prefixSymbols(i)}', " +
          s"Hash = '${minedHashes(i)}'"
      )
    }
    master.foreach(m => m ! PipelineFinished())
  }

  override def preStart(): Unit = log.info("Pipeline started")
  override def postStop(): Unit = log.info("Pipeline stopped")
}
