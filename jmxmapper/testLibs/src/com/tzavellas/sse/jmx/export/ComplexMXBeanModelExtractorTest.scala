package com.tzavellas.sse.jmx.export

import java.beans.ConstructorProperties

import javax.management.modelmbean.ModelMBeanAttributeInfo
import org.scalatest.FreeSpec

class ComplexMXBeanModelExtractorTest extends FreeSpec {

  case class Complex(
    string: String,
    stringMap: Map[String, String]
  )

  private val extractor = new SimpleMBeanModelExtractor

  "a" in {
    val attrs: Array[ModelMBeanAttributeInfo] = extractor.attributes(classOf[Complex])
    attrs.map {
      case m: ModelMBeanAttributeInfo if m.getName() == "string" =>
        assert(m.getType() === classOf[String].getName())
//      case m: ModelMBeanAttributeInfo if m.getName() == "stringMap" =>
//        assert(m.getType() === classOf[Map].getName())
      case m: ModelMBeanAttributeInfo =>
        println(s"unhandled: ${m}")
    }
  }

}
