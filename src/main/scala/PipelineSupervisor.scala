import GeneAnalysisService.{GenesWithId, IdToPartnerIdWithLength}
import HashMiningService.HashMiningRequest
import LinearCombinationService.LinearCombinationRequest
import PipelineSupervisor._
import PwCrackService.HashRangeCheckRequest
import Reader.{RawStudent, StudentsPath}
import akka.actor.{Actor, ActorLogging, Props}

object PipelineSupervisor  {
  val props  = Props(new PipelineSupervisor)

  final case class PipelineStart()
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

  override def receive: Receive = {
    case PipelineStart =>
      startPipeline()
    case StudentsData(data) =>
      storeStudents(data)
      startPwCrackService()
      startGeneAnalysisService()
    case CrackedPasswords(hashToPassword) =>
      storePasswords(hashToPassword)
      startLinearCombinationService()
    case IdToPartnerIdWithLength(studentIdToPartnerId) =>
      storePartners(studentIdToPartnerId)
      startHashMiningService()
    case LinearCombination(coefficients) =>
      storePrefixes(coefficients)
      startHashMiningService()
    case MinedHashes(hashes) =>
      storeMinedHashes(hashes)
  }

  def startPipeline(): Unit = {
    val reader = context.actorOf(Reader.props())
    reader ! StudentsPath("students.csv")
  }

  def startPwCrackService(): Unit = {
    val hashes = students.map(student => student.passwordHash)
    val pwCrackService = context.actorOf(Props[PwCrackService], "pwCrackService")
    pwCrackService ! HashRangeCheckRequest(hashes)
  }

  def startGeneAnalysisService(): Unit = {
    val genesWithId = students.map(student => (student.gene, student.id))
    val geneAnalysisService = context.actorOf(Props[GeneAnalysisService], "geneAnalysisService")
    geneAnalysisService ! GenesWithId(genesWithId)
  }

  def startLinearCombinationService(): Unit = {
    val linearCombinationService = context.actorOf(Props[LinearCombinationService], "linearCombinationService")
    linearCombinationService ! LinearCombinationRequest(passwords)
  }

  def startHashMiningService(): Unit = {
    if (prefixes.nonEmpty && partners.nonEmpty) {
      val hashMiningService = context.actorOf(Props[HashMiningService], name = "hashMiningService")
      hashMiningService ! HashMiningRequest(partners, prefixes)
    }
  }

  def storeStudents(students: Vector[Reader.RawStudent]): Unit = {
    this.students = students
    log.info("Student data received")
  }

  def storePasswords(hashToPassword: Map[String, String]): Unit = {
    passwords = students.map(student => hashToPassword(student.passwordHash))
    for ((RawStudent(_, name, _, _), i) <- students.zipWithIndex) {
      log.info(s"Password for student '$name': ${passwords(i)}")
    }
  }

  def storePartners(studentIdToPartnerId: Map[Int, (Int, Int)]): Unit = {
    partners = students.map(student => studentIdToPartnerId(student.id)._1)
    for ((RawStudent(_, name, _, _), i) <- students.zipWithIndex) {
      log.info(s"Password for student '$name': ${partners(i)}")
    }
  }

  def storePrefixes(coefficients: Vector[Int]): Unit = {
    prefixes = coefficients
    val prefixString = coefficients.map({ case 1 => '+' case -1 => '-' }).mkString("")
    log.info(s"Prefixes: $prefixString")
  }

  def storeMinedHashes(hashes: Vector[String]): Unit = {
    minedHashes = hashes
    for ((RawStudent(_, name, _, _), i) <- students.zipWithIndex) {
      log.info(s"Mined hash for student '$name': ${hashes(i)}")
    }
  }

  override def preStart(): Unit = log.info("Pipeline started")
  override def postStop(): Unit = log.info("Pipeline stopped")
}
