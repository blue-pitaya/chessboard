package example

import com.raquo.laminar.api.L._
import example.pages.ChessboardPage
import org.scalajs.dom

object Main extends App {
  val app = ChessboardPage.component()

  def containerNode = dom.document.querySelector("#app")
  render(containerNode, app)
}
