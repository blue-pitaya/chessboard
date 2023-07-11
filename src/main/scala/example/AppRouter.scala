package example

import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import org.scalajs.dom

sealed trait PageKey
object PageKey {
  case object Home extends PageKey
  case object BoardCreator extends PageKey
  case object Game extends PageKey

  def serialized(key: PageKey): String = key.toString()

  def deserialized(v: String): PageKey = v match {
    case v if v == "Home"         => Home
    case v if v == "BoardCreator" => BoardCreator
    case v if v == "Game"         => Game
  }

  def pageTitle(key: PageKey): String = "Chessboard"
}

object AppRouter {

  val homeRoute = Route.static(PageKey.Home, root / endOfSegments)
  val boardCreatorRoute = Route
    .static(PageKey.BoardCreator, root / "create_board" / endOfSegments)

  val router = new Router[PageKey](
    routes = List(homeRoute, boardCreatorRoute),
    getPageTitle = PageKey.pageTitle,
    serializePage = PageKey.serialized,
    deserializePage = PageKey.deserialized
  )(popStateEvents = windowEvents(_.onPopState), owner = unsafeWindowOwner)

  def navigateTo(page: PageKey): Binder[HtmlElement] = Binder { el =>
    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if (isLinkElement) {
      el.amend(href(router.absoluteUrlForPage(page)))
    }

    (
      onClick
        .filter(ev =>
          !(
            isLinkElement &&
              (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)
          )
        )
        .preventDefault --> (_ => router.pushState(page))
    ).bind(el)
  }

}
