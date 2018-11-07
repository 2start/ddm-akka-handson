import akka.actor.{Actor, ActorLogging, Props}
import scala.io.Source

object Reader {
  def props(): Props = Props[Reader]

  final case class StudentsPath(path: String)
  final case class RawStudent(id: String, name: String, password: String, gene: String)
}

class Reader extends Actor with ActorLogging{
  import Reader._

  override def receive: Receive = {
    case StudentsPath(path) =>
      val rawStudents = retrieveStudentsFromCsv(path)
      sender ! rawStudents
  }

  def retrieveStudentsFromCsv(path: String): Vector[RawStudent] = {
    val rawStudents = Source.fromFile(path).getLines()
      .drop(1)
      .filter(!_.isEmpty)
      .map(_.split(";"))
      .map(cols => RawStudent(cols(0), cols(1), cols(2), cols(3)))
      .toVector

    rawStudents
  }
}


