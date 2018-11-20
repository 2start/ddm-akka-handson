import LinearCombinator.LinearCombinationCheckRequest
import LinearCombinationService.LinearCombinationResponse
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class LinearCombinatorSpec extends TestKit(ActorSystem("handsonSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "LinearCombinationFinder actor" must {
    "find linear combination" in {
      val probe = TestProbe()
      val linearCombinationActor1 = system.actorOf(LinearCombinator.props)
      val linearCombinationActor2 = system.actorOf(LinearCombinator.props)

      val values = Vector(2, 5, 8, 4, 3)
      val coefficients1 = Vector(1)
      val coefficients2 = Vector(-1)

      linearCombinationActor1.tell(LinearCombinationCheckRequest(values, coefficients1), probe.ref)
      linearCombinationActor2.tell(LinearCombinationCheckRequest(values, coefficients2), probe.ref)

      val response = probe.expectMsgType[LinearCombinationResponse]

      val results = Vector(Vector(1, 1, -1, 1, -1), Vector(-1, -1, 1, -1, 1))

      results should contain (response.coefficients)
    }

    "find second linear combination" in {
      val probe = TestProbe()
      val linearCombinationActor1 = system.actorOf(LinearCombinator.props)
      val linearCombinationActor2 = system.actorOf(LinearCombinator.props)

      val values = Vector(5, 5)
      val coefficients1 = Vector(1)
      val coefficients2 = Vector(-1)

      linearCombinationActor1.tell(LinearCombinationCheckRequest(values, coefficients1), probe.ref)
      linearCombinationActor2.tell(LinearCombinationCheckRequest(values, coefficients2), probe.ref)

      val response = probe.expectMsgType[LinearCombinationResponse]

      val results = Vector(Vector(1, -1), Vector(-1, 1))

      results should contain (response.coefficients)
    }
  }

}
