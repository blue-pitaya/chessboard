package example

import cats.effect.IO
import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import example.pages.creator.CreatorPage
import example.pages.game.GamePage
import example.pages.home.HomePage
import org.http4s.client.Client
import org.http4s.dom._

object App {
  import AppModel._

  def create(): Element = {
    val client: Client[IO] = FetchClientBuilder[IO].create
    val appState = AppState.init
    val httpClient = new HttpClient(client)

    def splitRenderer = {
      SplitRender[PageKey, Element](AppRouter.router.currentPageSignal)
        .collectStatic(PageKey.Home) {
          HomePage.component()
        }
        .collectStatic(PageKey.BoardCreator) {
          CreatorPage.component(appState.dm, httpClient)
        }
        .collect[PageKey.Game] { key =>
          GamePage.component(key.id, appState.dm)
        }
    }

    div(child <-- splitRenderer.signal, appState.dm.documentBindings)
  }
}
