package example

import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.pages.ChessboardPage

object Main extends App {
  val app = ChessboardPage.component()

  def containerNode = dom.document.querySelector("#app")
  render(containerNode, app)
}
