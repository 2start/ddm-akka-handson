import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PipelineSupervisorSpec extends TestKit(ActorSystem("DeviceSpec")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll{

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PipelineSupervisor actor" must {
    "be created" in {
      val probe = TestProbe()
      val pipelineSupervisorActor = system.actorOf(PipelineSupervisor.props())

      //    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
      //    val response = probe.expectMsgType[Device.RespondTemperature]
      //    response.requestId should ===(42)
      //    response.value should ===(None)
    }
  }
}
