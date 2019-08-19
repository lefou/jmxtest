/* ------------------- sse-jmx ------------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.jmx.export

import java.lang.management.ManagementFactory

import com.tzavellas.sse.jmx.{IfAlreadyExists, JmxUtils, MBeanRegistrationSupport}
import javax.management.modelmbean.RequiredModelMBean
import javax.management.{MBeanServer, ObjectName}

/**
 * Exports objects to JMX.
 *
 * @param server          the MBeanServet to use of registering the objects
 * @param namingStrategy  consulted during the creation of the ObjectName
 * @param assembler       used to create MBean models for classes that are not MBeans
 * @param ifAlreadyExists what to do when a MBean with the same name is already registered
 */
final class MBeanExporter (
  private val assembler: MBeanInfoAssembler = MBeanInfoAssembler.default,
  val namingStrategy: ObjectNamingStrategy = ObjectNamingStrategies.default,
  private val ifAlreadyExists: IfAlreadyExists.Enum = IfAlreadyExists.Fail,
  val server: MBeanServer = ManagementFactory.getPlatformMBeanServer)
    extends MBeanRegistrationSupport {


  /**
   * Export the specified object to JMX.
   *
   * Same as `export(AnyRef,ObjectName)` except that the `ObjectName` is created
   * from the configured `namingStrategy`.
   *
   * @param ref the object to register
   */
  def export(ref: AnyRef): Unit =
    export(ref, namingStrategy(ref.getClass))

  /**
   * Remove the specified object from JMX.
   *
   * The configured `ObjectNamingStrategy` is consulted in order to find the
   * `ObjectName` of the specified object.
   *
   * @param ref    the object to remove from JMX.
   * @param ignore do not throw exception `ref` is not registered.
   */
  def remove(ref: AnyRef, ignore: Boolean = false): Unit =
    unregisterMBean(namingStrategy(ref.getClass))

  /**
   * Export the specified object to JMX.
   *
   * If the specified object is already an MBean then the specified object gets
   * registered. If the object is not an MBean then the configured
   * [[MBeanInfoAssembler]] is used to construct a `ModelMBeanInfo` and the
   * object gets registered as a `ModelMBean`.
   *
   * @param ref  the object to register
   * @param name the `ObjectName` to use for registering the object
   */
  def export(ref: AnyRef, name: ObjectName): Unit = {
    def modelMBean = {
      val info = new NoGetterAndSetterMBeanInfo(assembler.createMBeanInfo(ref.getClass))
      val model = new RequiredModelMBean(info)
      model.setManagedResource(ref, "ObjectReference")
      model
    }
    val mbean = if (JmxUtils.isMBean(ref.getClass)) ref else modelMBean
    registerMBean(mbean, name, ifAlreadyExists)
  }
}
