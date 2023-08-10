package macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Envs {
  def apiPathImpl(c: Context) = {
    import c.universe._
    val x = sys.env.get("API_PATH").getOrElse("http://localhost:8080/")
    c.Expr[String](q"$x")
  }

  def wsApiPathImpl(c: Context) = {
    import c.universe._
    val x = sys.env.get("WS_API_PATH").getOrElse("ws://localhost:8080/")
    c.Expr[String](q"$x")
  }

  def ApiPath: String = macro apiPathImpl
  def WsApiPath: String = macro wsApiPathImpl
}
