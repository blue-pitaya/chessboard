package example

import com.raquo.laminar.api.L._
import org.scalajs.dom

object Main extends App {
  val app = App.create()
  val containerNode = dom.document.querySelector("#app")

  render(containerNode, app)
}
