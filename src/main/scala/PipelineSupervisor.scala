import PasswordCrackerGroup.HashRangeCheckRequest
import Reader.{RawStudent, StudentsPath}
import akka.actor.{Actor, ActorLogging, Props}

object PipelineSupervisor {
  def props(): Props = Props(new PipelineSupervisor)

  final case class PipelineStart()
  final case class StudentsData(students: Vector[RawStudent])
  final case class HashRangeCheckResponse(crackedHashes: Map[String, String])
}

/**
  * A top level supervisor as recommended in https://doc.akka.io/docs/akka/current/guide/tutorial_2.html.
  */
class PipelineSupervisor extends Actor with ActorLogging {
  import PipelineSupervisor._

  override def preStart(): Unit = log.info("Pipeline started")
  override def postStop(): Unit = log.info("Pipeline stopped")

  var hashToStudentId = Map.empty[String, Integer]
  var studentList = Vector.empty[RawStudent]
  var numReportedPasswords = 0

  override def receive: Receive = {
    case PipelineStart =>
      startPipeline()
    case StudentsData(students) =>
      startPasswordCracker(students)
    case HashRangeCheckResponse(crackedHashes) =>
      reportPasswords(crackedHashes)
  }

  def startPipeline(): Unit = {
    val reader = context.actorOf(Reader.props())
    reader ! StudentsPath("students.csv")
  }

  def startPasswordCracker(students: Vector[RawStudent]): Unit = {
    studentList = students
    for ((student, index) <- students.zipWithIndex) {
      hashToStudentId += (student.passwordHash -> index)
    }
    val hashes = students.map(student => student.passwordHash)
    val crackerGroup = context.actorOf(PasswordCrackerGroup.props(this.self))
    crackerGroup ! HashRangeCheckRequest(hashes)
  }

  def reportPasswords(crackedHashes: Map[String, String]): Unit = {
    for ((hash, origin) <- crackedHashes) {
      val studentId = hashToStudentId(hash)
      val student = studentList(studentId)
      log.info(s"Password for student '${student.name}': $origin")
      numReportedPasswords += 1
    }
    if (numReportedPasswords == studentList.length)
      log.info("DONE")
  }
}