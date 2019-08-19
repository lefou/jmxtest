package example

import common4s.mapper.Mappers
import org.scalatest.FreeSpec

class EasyExample extends FreeSpec {

  "main" in {
    EasyExample.main(Array())
  }

}

/**
  * @author Kai Han
  */
object EasyExample {
  def main(args: Array[String]): Unit = {
    var map: collection.Map[String, Any] = Map("id" -> 9527L, "name" -> "Hank", "age" -> 21, "sex" -> 1.toShort, "rank" -> 100)

    var bean: AnyRef = Mappers.mapToBean[ScalaStudent](map)
    println(bean)

    bean = Mappers.mapToBean[JavaStudent](map)
    println(bean)

    map = Map("id" -> "9527", "name" -> "Hank", "age" -> "21", "sex" -> "1", "rank" -> "100")

    bean = Mappers.mapToBean[ScalaStudent](map, true)
    println(bean)

    map = Mappers.beanToMap(bean)
    println(map)

    println

    map = Map("id" -> 9527L, "name" -> "Hank", "age" -> 21, "sex" -> 1.toShort, "rank" -> 100)
    ExecTimeUtils.time("Mappers.mapToBean[ScalaStudent](map)", 1000000, 100000) {
      Mappers.mapToBean[ScalaStudent](map)
    }

    println

    ExecTimeUtils.time("Mappers.beanToMap(bean)", 1000000, 100000) {
      Mappers.beanToMap(bean)
    }
  }
}