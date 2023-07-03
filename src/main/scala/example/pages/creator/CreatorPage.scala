package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d
import example.pages.creator.logic.BoardUiLogic

object CreatorPage {
  def component(): HtmlElement = {
    val draggingModule = Dragging.createModule[Models.Piece]()

    val boardState = BoardUiLogic.State.default(Vec2d(6, 6))
    val boardUiObserver = BoardUiLogic.observer(boardState)

    div(
      cls("flex flex-row gap-4 w-full h-full m-4"),
      div("controls"),
      BoardContainer.component(boardState, boardUiObserver),
      PiecePicker.component(boardUiObserver, draggingModule),
      child <-- DraggingPieceContainer.componentSignal(boardState),
      draggingModule.documentBindings
    )
  }
}
