import PasswordCrackerGroup.{HashRangeCheckRequest, HashRangeCheckResponse}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PasswordCrackerGroupSpec extends TestKit(ActorSystem("PasswordCrackerGroupSpec")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PasswordCrackerGroup actor" must {
    "brute force passwords" in {
      val probe = TestProbe()
      val passwordCrackerGroup = system.actorOf(PasswordCrackerGroup.props(requestor = probe.ref))

      val hashes = Vector(
        "7c3c58cdfb7dbc141c28cba84d4d07ff67b936e913080142eed1c6f5bcb6c43f",
        "9ad8a9a2f51c5621bd1432d8f6dc33bf0cfa91889033d0a6f4d3f020d7c01037",
        "1ba1604f3c7d04016990169a1fc9716d425d092cd38a2431954bfa06449b1469",
        "5df65ebc5396fdab5e901e460436b2152a34ae94aa5dcd1671faaf9002699f25"
      )
      val hashRangeCheckRequest = HashRangeCheckRequest(hashes)
      passwordCrackerGroup ! hashRangeCheckRequest
      val response = probe.expectMsgType[HashRangeCheckResponse]
      response.crackedHashes.values should contain theSameElementsAs Vector("240492", "183998", "221100", "800375")
    }
  }

}
