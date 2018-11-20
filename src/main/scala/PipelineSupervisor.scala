import GeneAnalysisService.{GenesWithId, IdToPartnerIdWithLength}
import LinearCombinationService.LinearCombinationRequest
import PwCrackService.HashRangeCheckRequest
import Reader.{RawStudent, StudentsPath}
import akka.actor.{Actor, ActorLogging, Props}

object PipelineSupervisor {
  def props(): Props = Props(new PipelineSupervisor)

  final case class PipelineStart()
  final case class StudentsData(students: Vector[RawStudent])
  final case class CrackedPasswords(crackedHashes: Map[String, String])
  final case class LinearCombination(coefficients: Vector[Int])
  final case class MinedHashes(hashes: Vector[String])
}

/**
  * A top level supervisor as recommended in https://doc.akka.io/docs/akka/current/guide/tutorial_2.html.
  */
class PipelineSupervisor extends Actor with ActorLogging {
  import PipelineSupervisor._

  override def preStart(): Unit = log.info("Pipeline started")
  override def postStop(): Unit = log.info("Pipeline stopped")

  var students = Vector.empty[RawStudent]
  var hashToPassword = Map.empty[String, String]
  var idToPartnerId = Map.empty[Int, Int]
  var prefixes = Vector.empty[Int]

  override def receive: Receive = {
    case PipelineStart =>
      startPipeline()
    case StudentsData(students) =>
      this.students = students
      startPwCrackService()
      val tPostCrack = System.currentTimeMillis()
      //startGeneAnalysisService()
    case IdToPartnerIdWithLength(mapping) =>
      idToPartnerId = mapping.mapValues(_._1)
      reportPartners()
    case CrackedPasswords(crackedHashes) =>
      hashToPassword = crackedHashes
      println(s"Linear Combination start: ${System.currentTimeMillis()/1000}")
      startLinearCombinationService()
    case LinearCombination(coefficients) =>
      prefixes = coefficients
      reportPrefixes()
      println(s"Linear Combination end: ${System.currentTimeMillis()/1000}")
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

    // make sure that the passed pw vector is ordered by student ids (map does not guarantee ordering
    val orderedPasswords = students.map(s => hashToPassword(s.passwordHash))
    linearCombinationService ! LinearCombinationRequest(orderedPasswords)

  }

  def reportPasswords(): Unit = {
    for (RawStudent(_, name, passwordHash, _) <- students) {
      log.info(s"Password for student '$name': ${hashToPassword(passwordHash)}")
    }
  }

  def reportPartners(): Unit = {
    for (RawStudent(id, name, passwordHash, _) <- students) {
      log.info(s"Partner for student '$name' with id $id: ${idToPartnerId(id)}")
    }
  }

  def reportPrefixes(): Unit = {
    val prefixString = prefixes.map({case 1 => '+' case -1 => '-'}).mkString("")
    log.info(s"Prefixes: $prefixString")
  }
}