import akka.actor.{Actor, ActorLogging, Props}
import scala.io.Source

object Reader {
  def props() = Props[Reader]

  final case class StudentsPath(path: String)
  final case class RawStudent(id: String, name: String, password: String, gene: String)
}

class Reader extends Actor with ActorLogging{
  import Reader._

  override def receive: Receive = {
    case StudentsPath(path) =>
      val rawStudents = retrieveStudentsFromCsv(path)
  }

  def retrieveStudentsFromCsv(path: String): Vector[RawStudent] = {
    var rawStudents = Vector.empty[RawStudent]
    val bufferedSource = Source.fromFile(path)
    for (line <- bufferedSource.getLines) {
      val cols = line.split(";")//.map(_.trim)
      val rawStudent = RawStudent(cols(0), cols(1), cols(2), cols(3))
      rawStudents = rawStudents :+ rawStudent
    }
    bufferedSource.close

    rawStudents
  }
}


