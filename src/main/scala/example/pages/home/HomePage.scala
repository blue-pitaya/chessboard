package example.pages.home

import com.raquo.laminar.api.L._
import example.PageKey
import example.AppRouter
import example.Styles

object HomePage {
  def component(): HtmlElement = div(
    cls("flex flex-col gap-4 m-4"),
    h1(cls("text-3xl"), "Custom chess creator"),
    p("Create new kind of chess and play it with friends!"),
    button(
      Styles.btnCls,
      AppRouter.navigateTo(PageKey.BoardCreator),
      "Create new board"
    )
  )
}
