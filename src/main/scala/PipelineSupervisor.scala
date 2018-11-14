import GeneAnalysisService.{GenesWithId, IdToPartnerIdWithLength}
import PwCrackService.HashRangeCheckRequest
import Reader.{RawStudent, StudentsPath}
import akka.actor.{Actor, ActorLogging, Props}

object PipelineSupervisor {
  def props(): Props = Props(new PipelineSupervisor)

  final case class PipelineStart()
  final case class StudentsData(students: Vector[RawStudent])
  final case class CrackedPasswords(crackedHashes: Map[String, String])
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

  override def receive: Receive = {
    case PipelineStart =>
      startPipeline()
    case StudentsData(students) =>
      this.students = students
      startPwCrackService()
      startGeneAnalysisService()
    case CrackedPasswords(crackedHashes) =>
      hashToPassword = crackedHashes
      reportPasswords()
    case IdToPartnerIdWithLength(mapping) =>
      idToPartnerId = mapping.mapValues(_._1)
      reportPartners()
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

  def reportPasswords(): Unit = {
    for (RawStudent(_, name, passwordHash, _) <- students) {
      log.info(s"Password for student '${name}': ${hashToPassword(passwordHash)}")
    }
  }

  def reportPartners(): Unit = {
    for (RawStudent(id, name, passwordHash, _) <- students) {
      log.info(s"Partner for student '${name}' with id ${id}: ${idToPartnerId(id)}")
    }
  }
}