import PwCracker.{PasswordCheckRequest, PasswordCheckResponse}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PwCrackerSpec extends TestKit(ActorSystem("handsonSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PasswordCracker actor" must {
    "brute force password" in {
      val probe = TestProbe()
      val passwordCrackerActor = system.actorOf(PwCracker.props)

      passwordCrackerActor.tell(PasswordCheckRequest("7c3c58cdfb7dbc141c28cba84d4d07ff67b936e913080142eed1c6f5bcb6c43f", 111111, 999999), probe.ref)
      val response = probe.expectMsgType[PasswordCheckResponse]
      response.password.get should === ("240492")
    }
  }

}
