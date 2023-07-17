package example

import com.raquo.laminar.api.L._
import com.raquo.waypoint._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import org.scalajs.dom

sealed trait PageKey
object PageKey {
  case object Home extends PageKey
  case object BoardCreator extends PageKey
  case class Game(id: String) extends PageKey
  case object NotFound extends PageKey

  def serialized(key: PageKey): String = key.asJson.toString()

  def deserialized(v: String): PageKey = parser
    .decode[PageKey](v)
    .getOrElse(NotFound)

  def pageTitle(key: PageKey): String = "Chessboard"
}

object AppRouter {

  val router = {
    val homeRoute = Route.static(PageKey.Home, root / endOfSegments)
    val boardCreatorRoute = Route
      .static(PageKey.BoardCreator, root / "create_board" / endOfSegments)
    val gameRoute = Route[PageKey.Game, String](
      encode = _.id,
      decode = PageKey.Game(_),
      pattern = root / "game" / segment[String] / endOfSegments
    )
    val routes = List(homeRoute, boardCreatorRoute, gameRoute)

    new Router[PageKey](
      routes = routes,
      getPageTitle = PageKey.pageTitle,
      serializePage = PageKey.serialized,
      deserializePage = PageKey.deserialized
    )(popStateEvents = windowEvents(_.onPopState), owner = unsafeWindowOwner)
  }

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
