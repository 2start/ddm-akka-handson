import LcsCalculator.{LcsRequest, LcsResponse}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class LcsCalculatorSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PasswordCracker actor" must {
    "brute force password" in {
      val probe = TestProbe()
      val passwordCrackerActor = system.actorOf(Props[LcsCalculator])

      passwordCrackerActor.tell(LcsRequest(1, 2, "abab123456789sdkjfjasfdj", "adkfsj123456789xabxab"), probe.ref)
      val response = probe.expectMsgType[LcsResponse]
      response.length should be (9)
    }
  }

}
