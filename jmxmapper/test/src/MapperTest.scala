package jmxmapper

import java.lang.management.ManagementFactory

import scala.reflect.ClassTag
import scala.reflect.classTag

import com.tzavellas.sse.jmx
import com.tzavellas.sse.jmx.IfAlreadyExists
import javax.management.ObjectName
import javax.management.openmbean.{CompositeData, TabularData}
import org.scalatest.FreeSpec

trait MBeanTestSupport {

  lazy val mapper = new Mapper()

  def server = ManagementFactory.getPlatformMBeanServer();

  def attribute[T: ClassTag](name: String): AnyRef = {
    server.getAttribute(objectName[T], name)
  }

  def objectName[T: ClassTag] = {
    val runtimeClass = classTag[T].runtimeClass
    new ObjectName(s"${runtimeClass.getPackage().getName()}:type=${runtimeClass.getSimpleName()}")
  }

  def withExport[T <: Product : ClassTag](cc: T, replaceExisting: Boolean = true)(f: => Unit) = {
    val mbean = mapper.mapProduct(cc)
    val name = objectName[T]
    if (replaceExisting && server.isRegistered(name)) {
      server.unregisterMBean(name)
    }
    server.registerMBean(mbean, name)
    try {
      f
    } finally {
      server.unregisterMBean(name)
    }
  }

}

class MapperTest extends FreeSpec with MBeanTestSupport {

  import MapperTest._

  "Create mapping, export and read attributes" in {
    withExport(caseClass1) {
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

    support.withExport(caseClass1) {
      support.withExport(CaseClass2("cc2", caseClass1)) {
        println("Press any key...")
        System.in.read()
      }
    }

  }
}

