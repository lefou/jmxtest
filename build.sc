import mill._
import mill.api.Loose
import mill.define.Target
import mill.scalalib._
import mill.modules.Jvm

object jmxmapper extends ScalaModule {

  def scalaVersion = "2.12.9"

  def scalacOptions = Seq(
    "-deprecation"
  )

  override def ivyDeps = Agg(
    ivy"org.scala-lang:scala-reflect:${scalaVersion()}",
    ivy"org.javassist:javassist:3.20.0-GA"

  )

  object test extends Tests {

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.5",
      ivy"org.scalacheck::scalacheck:1.14.0"
    )

    def testFrameworks = Seq(
      "org.scalatest.tools.Framework"
    )

    def jmxArgs: T[Seq[String]] = T {
      Seq("-Dcom.sun.management.jmxremote")
    }

    def testJmx(mainClass: String = null, args: Seq[String] = Seq()) = T.command {
      println(s"Main class: ${mainClass}")
      Jvm.runSubprocess(
        mainClass = mainClass,
        runClasspath().map(_.path),
        jmxArgs() ++ forkArgs(),
        forkEnv(),
        args,
        workingDir = forkWorkingDir()
      )
    }

  }

}
