package jmxmapper

import java.lang.management.ManagementFactory

import scala.reflect.ClassTag
import scala.reflect.classTag

import com.tzavellas.sse.jmx
import com.tzavellas.sse.jmx.IfAlreadyExists
import com.tzavellas.sse.jmx.export.MBeanExporter
import com.tzavellas.sse.jmx.export.ObjectNamingStrategies.simpleNameOf
import javax.management.ObjectName
import openmbeanexample.SampleOpenMBean
import org.scalatest.FreeSpec

trait MBeanTestSupport {

  lazy val mapper = new Mapper()

  def exporter: MBeanExporter

  def server = exporter.server

  def attribute[T: ClassTag](name: String): AnyRef = {
    server.getAttribute(objectName[T], name)
  }

  def objectName[T: ClassTag] = exporter.namingStrategy(classTag[T].runtimeClass)

  def withExport[T <: Product : ClassTag](cc: T)(f: => Unit) = {
    val mbean = mapper.mapProduct(cc)
    val name = new ObjectName(s"${cc.getClass().getPackage().getName()}:type=${cc.getClass().getSimpleName()}")
    exporter.export(mbean, name)
    try {
      f
    } finally {
      server.unregisterMBean(name)
    }
  }

}

class MapperTest extends FreeSpec with MBeanTestSupport {

  import MapperTest._

  def exporter = new jmx.export.MBeanExporter(ifAlreadyExists = IfAlreadyExists.Replace)

  "Create mapping, export and read attributes" in {
    withExport(caseClass1) {
      assert(attribute[CaseClass]("aString") === caseClass1.aString)
      assert(attribute[CaseClass]("aInt") === caseClass1.aInt)
//      assert(attribute[CaseClass]("aStringArray") === caseClass1.aStringArray)
      val stringArrayAttr = attribute[CaseClass]("aStringArray")
    }
  }

}

object MapperTest {

  case class CaseClass(
                        aString: String,
                        aInt: Int,
                        aStringArray: Array[String],
                        aStringSeq: Seq[String]
                      )

  val caseClass1 = CaseClass(
    aString = "aString",
    aInt = 1,
    aStringArray = Array("s"),
    aStringSeq = Seq("s")
  )

  def main(args: Array[String]): Unit = {

    //    val mbs = ManagementFactory.getPlatformMBeanServer();

    import com.tzavellas.sse.jmx
    val exporter = new jmx.export.MBeanExporter(ifAlreadyExists = IfAlreadyExists.Replace)

    val mapper = new Mapper()
    val mbean = mapper.mapProduct(caseClass1)

    exporter.export(mbean)

    val sample = new SampleOpenMBean()
    exporter.export(sample)

    println("Press any key...")
    System.in.read()

  }
}

