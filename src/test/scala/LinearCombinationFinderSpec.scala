import LinearCombinationFinder.LinearCombinationCheckRequest
import LinearCombinationService.LinearCombinationFound
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class LinearCombinationFinderSpec extends TestKit(ActorSystem("handsonSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "LinearCombinationFinder actor" must {
    "find linear combination" in {
      val probe = TestProbe()
      val linearCombinationActor = system.actorOf(LinearCombinationFinder.props)

      val values = Vector(2, 5, 8, 4, 3)
      val coefficients = Vector(1)
      linearCombinationActor.tell(LinearCombinationCheckRequest(values, coefficients), probe.ref)
      val response = probe.expectMsgType[LinearCombinationFound]
      response.coefficients should === (Vector(1, 1, -1, 1, -1))
    }
  }

}
