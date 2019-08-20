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

  def withExport[T <: Product: ClassTag](cc: T)(f: => Unit) = {
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
      assert(attribute[CaseClass1]("aString") === caseClass1.aString)
      assert(attribute[CaseClass1]("aInt") === caseClass1.aInt)
      //      assert(attribute[CaseClass]("aStringArray") === caseClass1.aStringArray)
      val stringArrayAttr = attribute[CaseClass1]("aStringArray")
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

    val support = new MBeanTestSupport {
      override def exporter: MBeanExporter = new jmx.export.MBeanExporter(ifAlreadyExists = IfAlreadyExists.Replace)
    }

    support.withExport(caseClass1) {
      support.withExport(CaseClass2("cc2", caseClass1)) {
        println("Press any key...")
        System.in.read()
      }
    }

    //    val mapper = new Mapper()
    //    val mbean = mapper.mapProduct(caseClass1)
    //    exporter.export(mbean)
    //
    //    val mbean2 = mapper.mapProduct(CaseClass2("cc2", caseClass1))
    //  exporter.export(mbean2)
    //
    //    println("Press any key...")
    //    System.in.read()

  }
}

