package example.pages.creator

import com.raquo.laminar.api.L._
import example.game.Vec2d

object CreatorPage {
  def component(): HtmlElement = {
    val boardState = BoardContainer
      .State(boardLogicSize = Vec2d(20, 20), boardRealSize = Vec2d(800, 800))
    val state = State(boardState = boardState)
    val piecePickerObserver = Engine.handleEvent(state)

    div(
      cls("flex flex-row gap-4 w-full h-full m-4"),
      div("controls"),
      BoardContainer.component(boardState),
      PiecePicker.component(piecePickerObserver)
    )
  }
}
