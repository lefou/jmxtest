package jmxmapper

import javax.management.DynamicMBean

trait OpenMBeanMapper {

  /**
   * Maps a product (case class) to a Open MBean.
   * @param cc The case class.
   * @return The Open MBean.
   */
  def mapProduct(cc: Product): DynamicMBean

}