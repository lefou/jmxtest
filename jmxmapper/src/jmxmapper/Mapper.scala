package jmxmapper

import java.util.Date
import java.{lang => jl}

import scala.collection.JavaConverters._
import scala.collection.immutable

import javax.management.{DynamicMBean, MBeanNotificationInfo, ObjectName}
import javax.management.openmbean.{CompositeData, CompositeDataSupport, CompositeType, OpenMBeanAttributeInfo, OpenMBeanAttributeInfoSupport, OpenMBeanConstructorInfo, OpenMBeanInfoSupport, OpenMBeanOperationInfo, OpenType, SimpleType, TabularDataSupport, TabularType}

class Mapper() {
  type Element = (AnyRef, OpenType[_])

  def mapProduct(cc: Product): DynamicMBean = {
    val elements = productToMap(cc)

    println(s"Case class: ${cc} ==> ${elements}")

    val openAttributes: Array[OpenMBeanAttributeInfo] = elements.map{e =>
      new OpenMBeanAttributeInfoSupport(e._1, e._1, e._2._2, true, false, false)
    }.toArray[OpenMBeanAttributeInfo]
    val openConstructors: Array[OpenMBeanConstructorInfo] = Array()
    val openOperations: Array[OpenMBeanOperationInfo] = Array()
    val notifications: Array[MBeanNotificationInfo] = Array()

    val mBeanInfo = new OpenMBeanInfoSupport(
      cc.getClass().getName(),
      s"Generic MBean for class ${cc.getClass().getName()}",
      openAttributes,
      openConstructors,
      openOperations,
      notifications
    )

    val mbean = new GenericImmutableOpenMBean(mBeanInfo, elements)
    mbean
  }

  def productToMap(cc: Product): Map[String, Element] = {
    val values: Iterator[Any] = cc.productIterator
    cc.getClass.getDeclaredFields.filter { f =>
      f.getName != "$outer"
    }.map { f =>
      println(s"processing field of [${cc}]: ${f}")
      val value = values.next()
      println(s"    value of field [${f}]: [${value}]")
      val element = fieldToElement(f.getName, value)
      f.getName -> element
    }.toMap
  }

  def fieldToElement(name: String, field: Any): Element = {

    object SeqMatcher {
      def unapply(arg: Any): Option[Iterable[_]] = arg match {
        case x: Seq[_] => Some(x)
        case x: Set[_] => Some(x)
        case x: Array[_] => Some(x.toSeq)
        case _ => None
      }
    }

    field match {
      //        case x if x != null && x.getClass().isPrimitive() =>

      case x: Unit => Void.TYPE -> SimpleType.VOID
      case x: Void => Void.TYPE -> SimpleType.VOID
      case x: Boolean => Boolean.box(x) -> SimpleType.BOOLEAN
      case x: jl.Boolean => x -> SimpleType.BOOLEAN
      case x: Char => Char.box(x) -> SimpleType.CHARACTER
      case x: jl.Character => x -> SimpleType.CHARACTER
      case x: Byte => Byte.box(x) -> SimpleType.BYTE
      case x: jl.Byte => x -> SimpleType.BYTE
      case x: Short => Short.box(x) -> SimpleType.SHORT
      case x: jl.Short => x -> SimpleType.SHORT
      case x: Int => Int.box(x) -> SimpleType.INTEGER
      case x: jl.Integer => x -> SimpleType.INTEGER
      case x: Long => Long.box(x) -> SimpleType.LONG
      case x: jl.Long => x -> SimpleType.LONG
      case x: Float => Float.box(x) -> SimpleType.FLOAT
      case x: jl.Float => x -> SimpleType.FLOAT
      case x: Double => Double.box(x) -> SimpleType.DOUBLE
      case x: jl.Double => x -> SimpleType.DOUBLE
      case x: String => x -> SimpleType.STRING
      case x: BigDecimal => x.bigDecimal -> SimpleType.BIGDECIMAL
      case x: BigInt => x.bigInteger -> SimpleType.BIGINTEGER
      case x: Date => x -> SimpleType.DATE
      case x: ObjectName => x -> SimpleType.OBJECTNAME

      // we are empty and readonly, so the element type doesn't matter
      case SeqMatcher(x) if x.isEmpty => Void.TYPE -> SimpleType.VOID

      // inspect first element
      case SeqMatcher(x) if !x.isEmpty =>
        val element: (Any, OpenType[_]) = fieldToElement(name, x.head)
        val rowType = new CompositeType(name, name, Array(name), Array(name), Array(element._2))
        val openType = new TabularType(name, name, rowType, Array(name))

        val value = new TabularDataSupport(openType)
        val allValues = x.map { e =>
          val data = fieldToElement(name, e)
          new CompositeDataSupport(rowType, Array(name), Array[AnyRef](data._1))
        }
        value.putAll(allValues.toArray[CompositeData])

        // TODO: fill data
        value -> openType

      case x: Product =>
        val fields: Map[String, Element] = productToMap(x)
        val names = fields.map(_._1).toArray
        val types = fields.map(_._2._2).toArray

        val openType = new CompositeType(
          name,
          name,
          names.toArray,
          names.toArray,
          types.toArray
        )

        val value = new CompositeDataSupport(openType, fields.mapValues(_._1).asJava)

        value -> openType
    }

  }

}