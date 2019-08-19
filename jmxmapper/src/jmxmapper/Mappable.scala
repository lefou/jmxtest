package jmxmapper

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait Mappable[T] {
  def toMap(t: T): Map[String, Any]
  def fromMap(map: Map[String, Any]): T
}

object Mappable {

  def mapify[T: Mappable](t: T): Map[String, Any] = implicitly[Mappable[T]].toMap(t)

  def materialize[T: Mappable](map: Map[String, Any]): T = implicitly[Mappable[T]].fromMap(map)

  implicit def materializeMappable[T]: Mappable[T] = macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companion

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val (toMapParams, fromMapParams) = fields.map { field =>
      val name = field.name.toTermName
      val decodedName = name.decodedName.toTermName
      val returnType = tpe.decl(name).typeSignature

      (q"${decodedName} → t.${name}", q"map($decodedName).asInstanceOf[$returnType]")
    }.unzip

    c.Expr[Mappable[T]] {
      q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe): Map[String, Any] = Map(..$toMapParams)
        def fromMap(map: Map[String, Any]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}