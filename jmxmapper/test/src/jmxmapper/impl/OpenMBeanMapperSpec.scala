package jmxmapper.impl

import java.{lang => jl}
import java.{math => jm}

import scala.reflect.{ClassTag, classTag}
import scala.util.{Success, Try}

import javax.management.ObjectName
import javax.management.openmbean.{ArrayType, CompositeData, OpenType, SimpleType, TabularData}
import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.prop.PropertyChecks
import org.scalacheck.{Arbitrary, Gen}

class OpenMBeanMapperSpec extends FreeSpec with MBeanTestSupport with PropertyChecks with Matchers {

  import OpenMBeanMapperSpec._

  implicit val arbObjectNames: Arbitrary[ObjectName] = Arbitrary {
    for {
      domain <- Gen.identifier
      name <- Gen.identifier
    } yield new ObjectName(s"${domain}:type=${name}")
  }

  implicit val arbJmBigDecimal: Arbitrary[jm.BigDecimal] = Arbitrary {
    for (b <- Arbitrary.arbitrary[BigDecimal]) yield b.bigDecimal
  }
  implicit val arbJmBigInteger: Arbitrary[jm.BigInteger] = Arbitrary {
    for (b <- Arbitrary.arbitrary[BigInt]) yield b.bigInteger
  }

  "The OpenMBeanMapperImpl should" - {

    val mapper = new OpenMBeanMapperImpl()

    "map standard Java types and primitives" - {
      def testMapping[T: ClassTag](`type`: OpenType[_], box: T => _ = null)(implicit arb: Arbitrary[T]): Unit = {
        s"of type ${`type`}" in {
          forAll { d: T =>
            assert(mapper.fieldToElement("prim", d) === (d, `type`))
            Option(box).foreach { b =>
              val boxed = b(d)
              assert(mapper.fieldToElement("boxed", boxed) === (boxed, `type`))
            }
          }
        }
      }

      testMapping(SimpleType.BOOLEAN, Boolean.box)
      testMapping(SimpleType.BYTE, Byte.box)
      testMapping(SimpleType.SHORT, Short.box)
      testMapping(SimpleType.INTEGER, Int.box)
      testMapping(SimpleType.LONG, Long.box)
      testMapping(SimpleType.FLOAT, Float.box)
      testMapping(SimpleType.DOUBLE, Double.box)
      testMapping[String](SimpleType.STRING)
      testMapping[jm.BigDecimal](SimpleType.BIGDECIMAL)
      testMapping[jm.BigInteger](SimpleType.BIGINTEGER)
      testMapping[ObjectName](SimpleType.OBJECTNAME)
    }

    "map Scala seqs" - {
      def testMapping[T: ClassTag](`type`: OpenType[_])(implicit arb: Arbitrary[T]): Unit = {
        s"of ${`type`}" in {
          forAll { d: Seq[T] =>
            println(s"${`type`} -> ${d}")
            if (d.isEmpty) {
              // we do not infer types for empty Seq's
              assert(mapper.fieldToElement("prim", d) === (Void.TYPE, SimpleType.VOID))
            } else {
              assert(mapper.fieldToElement("prim", d) === (d, `type`))
            }
          }
        }
      }

      //      testMapping[Boolean](SimpleType.BOOLEAN)
      pending
      //      testMapping(Byte.box, SimpleType.BYTE)
      //      testMapping(Short.box, SimpleType.SHORT)
      //      testMapping(Int.box, SimpleType.INTEGER)
      //      testMapping(Long.box, SimpleType.LONG)
      //      testMapping(Float.box, SimpleType.FLOAT)
      //      testMapping(Double.box, SimpleType.DOUBLE)
    }

    "map Scala maps" in {
      pending
    }

    "map Java arrays" - {
      def testMapping[T: ClassTag, R: ClassTag](type0: OpenType[_], box: T => R = null)(implicit arb: Arbitrary[T]): Unit = {
        val `type` = new ArrayType(1, type0)
        s"of type ${`type`}" in {
          forAll { d: Array[T] =>
            Option(box) match {
              case None =>
                assert(mapper.fieldToElement("prim", d) === (d, `type`))
              case Some(b) =>
                val boxed = d.map(b)
                assert(mapper.fieldToElement("boxed", boxed) === (boxed, `type`))
              //                assert(mapper.fieldToElement("prim-to-boxed", d) === (boxed, `type`))
            }
          }
        }
      }

      testMapping(SimpleType.BOOLEAN, Boolean.box)
      testMapping(SimpleType.BYTE, Byte.box)
      testMapping(SimpleType.SHORT, Short.box)
      testMapping(SimpleType.INTEGER, Int.box)
      testMapping(SimpleType.LONG, Long.box)
      testMapping(SimpleType.FLOAT, Float.box)
      testMapping(SimpleType.DOUBLE, Double.box)
      testMapping[String, String](SimpleType.STRING)
      //      testMapping[jm.BigDecimal](SimpleType.BIGDECIMAL)
      //      testMapping[jm.BigInteger](SimpleType.BIGINTEGER)
      //      testMapping[ObjectName](SimpleType.OBJECTNAME)
    }

    "map Java collections" in {
      pending
    }

    "map Java maps" in {
      pending
    }

  }

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

object OpenMBeanMapperSpec {

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

