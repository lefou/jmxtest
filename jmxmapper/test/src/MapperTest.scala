package jmxmapper

import javax.management.openmbean.{CompositeData, TabularData}
import org.scalatest.FreeSpec

class MapperTest extends FreeSpec with MBeanTestSupport {

  import MapperTest._

  "Create mapping, export and read attributes" in {
    withMappedExport(caseClass1) {
      assert(attribute[CaseClass1]("aString") === caseClass1.aString)
      assert(attribute[CaseClass1]("aInt") === caseClass1.aInt)

      val stringArrayAttr = attribute[CaseClass1]("aStringArray")
      assert(stringArrayAttr === caseClass1.aStringArray)

      val stringSeqAttr = attribute[CaseClass1]("aStringSeq")
      assert(stringSeqAttr.isInstanceOf[TabularData])
      //      assert(stringSeqAttr.size() === 1)
      val data = stringSeqAttr.asInstanceOf[TabularData].values()
      assert(data.size() === 1)
      assert(data.iterator().next().isInstanceOf[CompositeData])
    }
  }

}

object MapperTest {

  case class CaseClass1(
    aString: String,
    aInt: Int,
    aStringArray: Array[String],
    aStringSeq: Seq[String]
  )

  val caseClass1 = CaseClass1(
    aString = "aString",
    aInt = 1,
    aStringArray = Array("s"),
    aStringSeq = Seq("s")
  )

  case class CaseClass2(
    name: String,
    cc1: CaseClass1
  )

  def main(args: Array[String]): Unit = {

    val support = new MBeanTestSupport {}

    support.withMappedExport(caseClass1) {
      support.withMappedExport(CaseClass2("cc2", caseClass1)) {
        println("Press any key...")
        System.in.read()
      }
    }

  }
}

