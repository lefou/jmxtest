package common4s.mapper

/**
 * @author Kai Han
 */
private[mapper] case class ArgWithDefault(paramType : Class[_] = classOf[Object], paramName : String, defaultValue : Any)