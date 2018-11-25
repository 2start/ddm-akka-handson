import PipelineSupervisor.StudentsData
import akka.actor.{Actor, ActorLogging, Props}

import scala.io.Source

object Reader {
  def props(): Props = Props[Reader]

  final case class StudentsPath(path: String)
  final case class RawStudent(id: Int, name: String, passwordHash: String, gene: String)
}

class Reader extends Actor with ActorLogging{
  import Reader._

  override def receive: Receive = {
    case StudentsPath(path) =>
      sender ! StudentsData(retrieveStudentsFromCsv(path))
  }

  def retrieveStudentsFromCsv(path: String): Vector[RawStudent] = {
    Source.fromFile(path).getLines()
      .drop(1)
      .filter(!_.isEmpty)
      .map(_.split(";"))
      .map(cols => RawStudent(cols(0).toInt, cols(1), cols(2), cols(3)))
      .toVector
  }
}


