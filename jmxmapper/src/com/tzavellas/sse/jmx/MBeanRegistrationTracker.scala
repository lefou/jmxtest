/* ------------------- sse-jmx ------------------- *\
 * Licensed under the Apache License, Version 2.0. *
 * Author: Spiros Tzavellas                        *
\* ----------------------------------------------- */
package com.tzavellas.sse.jmx

import java.util.concurrent.ConcurrentHashMap

import javax.management.{InstanceNotFoundException, ObjectName}

/**
 * Tracks MBean registrations.
 */
trait MBeanRegistrationTracker extends MBeanRegistrationSupport {

  private val registered = new ConcurrentHashMap[ObjectName, Unit]

  abstract override def registerMBean(mbean: AnyRef, name: ObjectName, behavior: IfAlreadyExists.Enum = IfAlreadyExists.Fail): Unit = {
    super.registerMBean(mbean, name, behavior)
    registered.putIfAbsent(name, ())
  }

  abstract override def unregisterMBean(name: ObjectName, ignore: Boolean = false): Unit = {
    super.unregisterMBean(name)
    registered.remove(name)
  }

  /**
   * Unregister all the MBeans that have been registered using this instance.
   *
   * @see [[registerMBean]]
   */
  def unregisterAll(): Unit = {
    val names = registered.keySet.iterator
    while(names.hasNext) {
      try {
        unregisterMBean(names.next)
      } catch {
        case e: InstanceNotFoundException =>
      }
      names.remove()
    }
  }
}
