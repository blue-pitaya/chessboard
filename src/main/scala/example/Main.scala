package example

import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.pages.home.HomePage
import com.raquo.waypoint._
import example.pages.creator.CreatorPage

object Main extends App {

  def splitRenderer =
    SplitRender[PageKey, HtmlElement](AppRouter.router.currentPageSignal)
      .collectStatic(PageKey.Home) {
        HomePage.component()
      }
      .collectStatic(PageKey.BoardCreator) {
        CreatorPage.component()
      }

  def containerNode = dom.document.querySelector("#app")
  val app = div(child <-- splitRenderer.signal)

  render(containerNode, app)

}
