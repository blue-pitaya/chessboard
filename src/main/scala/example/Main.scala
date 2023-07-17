package example

import cats.effect.IO
import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import example.pages.creator.CreatorPage
import example.pages.home.HomePage
import org.http4s.client.Client
import org.http4s.dom._
import org.scalajs.dom
import example.pages.game.GamePage
import example.pages.game.GamePage2

object Main extends App {
  val client: Client[IO] = FetchClientBuilder[IO].create
  val appState = AppModel.AppState.init
  val httpClient = new HttpClient(client)

  // TODO:
  private def createApp() = {
    ???
  }

  def splitRenderer = {
    SplitRender[PageKey, Element](AppRouter.router.currentPageSignal)
      .collectStatic(PageKey.Home) {
        HomePage.component()
      }
      .collectStatic(PageKey.BoardCreator) {
        CreatorPage.component(appState.dm)
      }
      .collect[PageKey.Game] { key =>
        // val _fetchGameInfo = httpClient.fetchGameInfo(key.id)
        // val gameWebSocket = httpClient.gameWebSocket(key.id)
        // GamePage.component(appState.dm, _fetchGameInfo, gameWebSocket)

        GamePage2.component()
      }
  }

  def containerNode = dom.document.querySelector("#app")
  val app = div(child <-- splitRenderer.signal, appState.dm.documentBindings)

  render(containerNode, app)

}
