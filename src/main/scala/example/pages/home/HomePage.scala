package example.pages.home

import com.raquo.laminar.api.L._
import example.PageKey
import example.AppRouter

object HomePage {
  def component(): HtmlElement = div(
    cls("flex flex-col"),
    a(AppRouter.navigateTo(PageKey.BoardCreator), "Create template")
  )
}
