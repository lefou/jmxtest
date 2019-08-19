package jmxmapper

import java.lang.management.ManagementFactory

import com.tzavellas.sse.jmx.IfAlreadyExists
import javax.management.ObjectName
import org.scalatest.FreeSpec

class Test2 extends FreeSpec {

  "test 1" in {


  }

}


trait HelloMBean {
  def setMessage(message: String): Unit

  def getMessage(): String

  def sayHello(): Unit
}

class Hello() extends HelloMBean {
  private[this] var message = "Hello, world"

  def this(message: String) = {
    this()
    this.message = message
  }

  def setMessage(message: String): Unit = this.message = message

  def getMessage(): String = message

  def sayHello(): Unit = {
    println(message)
  }
}


object Main {

  def main(args: Array[String]): Unit = {

    val mbs = ManagementFactory.getPlatformMBeanServer();

    val helloBean = new Hello()

    mbs.registerMBean(helloBean, new ObjectName("Foo:name=HelloBean"))

    //    Thread.sleep(100000)
    println("Press any key...")
    System.in.read()
  }

}

object Main2 {

  def main(args: Array[String]): Unit = {

    val mbs = ManagementFactory.getPlatformMBeanServer();

    val bean = TestBean("test1", Map("opt1" -> "val1", "opt2" -> "val2"))

    import com.tzavellas.sse.jmx

    val exporter = new jmx.export.MBeanExporter(ifAlreadyExists = IfAlreadyExists.Replace)
    exporter.export(bean)

    //    Thread.sleep(100000)
    println("Press any key...")
    System.in.read()
  }

}

case class TestBean(name: String, options: Map[String, String])