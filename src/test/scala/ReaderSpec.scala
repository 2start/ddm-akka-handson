import Reader.{RawStudent, StudentsPath}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ReaderSpec extends TestKit(ActorSystem("testSystem")) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Reader actor" must {
    "read students from the astudents.csv" in {
      val probe = TestProbe()
      val readerActor = system.actorOf(Reader.props())

      val path = getClass.getResource("/students.csv").getPath

      readerActor.tell(StudentsPath(path), probe.ref)

      val response = probe.expectMsgType[Vector[RawStudent]]


      response should contain (RawStudent(
       "1",
       "Sophia",
        "7c3c58cdfb7dbc141c28cba84d4d07ff67b936e913080142eed1c6f5bcb6c43f",
        "GGUCUCGAAGGGUGAACAAGCGACCUCAGAUCGUUGGCCUUCACCCGCACAGCGGUUGCCGCGU" +
          "AUAAGGGGUAGGGAUACAUUUACCUGCAACUGACCCUGUCACAUCUAGUCCCUUGUUCUGCU" +
          "GCCGCGCAAUUGUUCAUCGUUACGAUUAGUGGAGGACCUAACCUCAGCUGUCUGUUGGGUGA" +
          "UAACUGUUUGGAGUCUUCUGCGACCCCCGGAACCGUGUCUCUCCGUCAGCGCUUCCCCAAUG" +
          "UCCGGCAACCGUAGCUCGUCCCAAGUCUUUUGAGACAUAUAUCGAUGGCCAGCACCCGAAUC" +
          "CUGUCCAGGGGGAUAGCAAGUUCUUCUUGGCAACAGAGCAAGAAUUGGACAUAUUGCCAUAA" +
          "ACAUUACCCGACUAACCCAGCAGCGAGUCAAAAGAGCGUAGUCCCUAUACGCCCCCGGCUGC" +
          "AUUUAGUCGGAGAACAUCAGAUAAAAACGCCGUUUGACACGUAUAAGUCCCGCUCACUAUAU" +
          "AUAGUCGGCAUAGAGUAUCGGUAUCUGCGGUAUGAGGUGAAACCUUCUUAGAGACAGCGACG" +
          "UUUUGAUUAUGGGAUGCUGAUCACGAGAGUGUCACGGAAUGUACUCCUCCAAGGCCGCGUUA" +
          "GAGUGCGGACGUAUGUACUAUGAUCAAUUAGGGCCAUGCAUUCACAAUUCUAACUCGGCGUG" +
          "GGGCGGUAACUCAAGCCUGCCUAGCUCUACGUUACGUAGUCGUCGCUUGAUGGGUGACCUAA" +
          "GACAGGGUGGAGAGAAAAUCGAUGCUACUAAGUACGAAUAGAAGGGAGGUGUCGGUAGGUAC" +
          "GUUGAACGGUACCGCUUUGAUUUUUUAACUUUAUCGUAUCUCCUGCUUGUUUAAAGCGCAAG" +
          "UGGAGGUUUUGGGAUGAUUAGAAAGUAGUUCAUAUGUCUUAAAGUCGAGCGUCCCCAGGCGU" +
          "AUGCCUAUAACCGCAGCUAAUCUGCAUUCACUGGCGGUACCCUUUCGGGUUCGUUUUAGGUC" +
          "GAUCCAAGUACUAAACGAGUUGCGAUAUGAGUUGUUUAGAGAAAAGUGGUCGUACAAUACAA" +
          "AUUAUCCUUUCCCUCACAAUGUCUAGGUUUAUCCACGCCUAACUCUAGUCCCAGCUUGUUCG" +
          "UCUUGUAAUGAAGCUGACAGCGAACUCUCCAGCAAUUUUUUCACCCAAUCCUGAAUUAGAGA" +
          "CCGACCAGGCGGGAGGCUACAUCUGGAUGUCUAAGGGCCUGAGGACGCCUAUUCGCUUAUGU" +
          "CUCUUACUCCGUGUUCGUUGCCCAUUAUGAUCUUGCCAUAUUCAUGCUCACCGCAUUGCCAA" +
          "CGGCCGGUUAACCGCCGAAGGUUGCGGGGCCAACCGCGCUGAUGAGUUUAUUAUACGCUUCC" +
          "CACCCGCCCUCCGGCAGUCGACUCCUGACCUUCUGUCUAUCGCUGCAGCACGACGCUGCAUA" +
          "UUGGAACACUCGCCCGCAGAGGAGAGUUGCUUGCCUCUUGCAAAAUGAAAGUGGCAGAGGCG" +
          "CGUUGAUUAAACGCAGCUAUUGGAUGAAAACGUCUACUUAUCGUCCUUGAUCGGGAUGAGGC" +
          "UCACGACAACUGACCCCGUGUGAAGCUAAUAUCCGUGGAGGUUCAGACCGGCUGCUCCAAAU" +
          "GAACUCCUUACGAGCUGGGGUUAUCAUAAUUACGCACUUGACCGAUAGACUCUGCGGAAUGC" +
          "ACGAUUAUCUGCGAGCCGAGUGCCCUUUUUCUGGGUAGCGAUCACUCGCACACGUCCACCCU" +
          "AAAAGCUCAGUCGACUUGGCACUAGCCUUGGCAACAGAGUCAUAGGUAUAUAUGUACUUUUA" +
          "UCUCUCAGCCUGAAAAAACCAGGAGACGUGUAAAUAGUGUAGUUCUACUCUGGACGGAACCU" +
          "UUUUCGACAGUGCCUCAAAACCUAUUGACAAUUUGAUCCAUUGGACGGACCGGGUGCCGGCU" +
          "GCGCCGCGCUGAUGUUAAUGCCCGAACACACGCACAAUACACGCCUGCUAGGGCACCCUAAA" +
          "GAACAUCAGCAGAACGAUCGUGGGUUGCCCAUACGGACGACCCCGCACGUAACAAAGACACU" +
          "AUCUACGUCCGAAGAGGAGUGAUAAUUUGCAUUACCUAAAGCCCGUGAAGCUUUAUAGAAUC" +
          "CUUGUUUGGCCGUACCCCCGAGCUCUACGCCCCAAUAAUACAAUGUGGUACUAUAUUAGUGU" +
          "UUCGCUGGAGUGAGCCUAGCCCAAACCCGUUUAGGAGCACUUAUUGGGCCUUAGUACGCUAC" +
          "UAGUAAUGAUAGCUAUGGCAGGAAAGGUACGACAAAAUGUACAGUCAUUAGAGCCGAGCUUC" +
          "CCGUGGGCUGACUGGCGCCCUAGUUGAUCACAGUAGCGCUUGCCACCCUAGCAAGUCAAUGC" +
          "UGGUCAAGGAGAGUCUUCGGGCCCGAUCAACCGGUUGAGAUCCACAGACAUCUAAACAUGGU" +
          "UUGUGUACUGGCCUACUCGGGUUACGAAGUCACGGGCAUUACAUAAUUGUAGAGGGCUUGAC" +
          "UAGGCCAAUGAUAUCGAAUUUUUAGAUGUCCUGAGCCCCCGAGAGACAACACUCCUAGCGAU" +
          "UGACGCUCUUCAGUCGCAAUUGAGACCACCCUGACAAAACCUCGCCCCGGCCCCGGAGUUCA" +
          "UGACGCUCACAGUUGUCGACAGCUCUUACGAAACACCGAUCCGUUGAGCUACUCAGAGGCCA" +
          "CGGCGACGCACCGAGACAGCCAGACUUGAAUACACAGUCGAAGGAUAUUUCCAGUGUAUUAG" +
          "GAUUGCUACGCUUCGUUCUAAAGCAUUGUUCUAGUUCUCCAAGGGAACGAGGGCAAAAAUAC" +
          "GGGGGGAAUUGACCUUCGGGCAGAUGAUGCGCCUCCUGGACGGUAAUGUUGAUGGAGCCAAU" +
          "UAUCACUGAGCAAGCACACACUUUUUCUACUUUGUUUGAAAUCAGCUAGGAGACAUCCUAAC" +
          "CUGACUGCCAGAGACUCAGAGUCUGUUUGAGCAAGCCACGAGGUUGUGCACAAGUCAAGCUU" +
          "ACGCAAUUCCUGUGUCGCCGCA"
      ))
    }
  }
}
