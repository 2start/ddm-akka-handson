import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PipelineSupervisorSpec extends TestKit(ActorSystem("DeviceSpec")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PipelineSupervisor actor" must {
    "be created" in {
      val probe = TestProbe()
      val pipelineSupervisorActor = system.actorOf(PipelineSupervisor.props())
    }
  }
}
