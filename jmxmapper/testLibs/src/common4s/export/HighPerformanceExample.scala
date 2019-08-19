package example

import scala.collection.JavaConverters._

import common4s.mapper.{BeanToMapMapper, MapToBeanMapper}
import org.scalatest.FreeSpec

class HighPerformanceExample extends FreeSpec {

  import HighPerformanceExample._

  "benchmark map-to-bean" in {
    mapToBeanBeanchmark()
  }

  "benchmark bean-to-map" in {
    beanToMapBeanchmark()
  }

}

/**
  * @author Kai Han
  */
object HighPerformanceExample {
  val mapToBeanMapper = MapToBeanMapper.createMapper(classOf[ScalaStudent])
  val beanToMapMapper = BeanToMapMapper.createMapper(classOf[ScalaStudent])

  def main(args: Array[String]): Unit = {
    mapToBeanBeanchmark()
    println
    beanToMapBeanchmark()
  }

  def mapToBeanBeanchmark() = {
    val map = Map("id" -> 9527L, "name" -> "Hank", "age" -> 21, "sex" -> 1.toShort, "rank" -> 100)
    val jmap = map.asJava

    ExecTimeUtils.time("MapToBeanMapper", 1000000, 100000) {
      mapToBeanMapper.map(jmap)
    }
  }

  def beanToMapBeanchmark() = {
    val bean = ScalaStudent(9527, "Hank", 21, 1, 100)

    ExecTimeUtils.time("BeanToMapMapper", 1000000, 100000) {
      beanToMapMapper.map(bean)
    }
  }

}

