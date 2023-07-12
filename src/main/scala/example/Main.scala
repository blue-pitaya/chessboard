package example

import cats.effect.IO
import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import example.pages.creator.CreatorPage
import example.pages.home.HomePage
import org.http4s._
import org.http4s.dom._
import org.http4s.implicits._
import org.scalajs.dom
import cats.effect.unsafe.implicits.global

object Main extends App {
  val client = FetchClientBuilder[IO].create

  def testMe() = {
    val request = Request[IO](
      method = Method.GET,
      uri = uri"http://localhost:8080/chessboard"
    )

    client.expect[String](request).flatMap(IO.println)
  }

  testMe().unsafeRunAndForget()

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
