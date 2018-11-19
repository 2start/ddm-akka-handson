import LcsCalculator.{LcsRequest, LcsResponse}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class LcsCalculatorSpec extends TestKit(ActorSystem("handsonSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "LcsCalculator actor" must {
    "find longest common string" in {
      val probe = TestProbe()
      val lcsCalculatorActor = system.actorOf(Props[LcsCalculator])

      lcsCalculatorActor.tell(LcsRequest(1, 2, "abab123456789sdkjfjasfdj", "adkfsj123456789xabxab"), probe.ref)
      val response = probe.expectMsgType[LcsResponse]
      response.length should be (9)
    }
  }

}
