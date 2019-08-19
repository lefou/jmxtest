package jmxmapper

import java.lang.management.ManagementFactory

import com.tzavellas.sse.jmx.IfAlreadyExists
import org.scalatest.FreeSpec

class MapperTest extends FreeSpec {

  import MapperTest._

  "1" in {

    val mapper = new Mapper()
    val mbean = mapper.mapProduct(caseClass1)

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

    println("Press any key...")
    System.in.read()

  }
}

