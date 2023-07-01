package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d

object CreatorPage {
  def component(): HtmlElement = {
    val state = State.init(Vec2d(10, 10))
    val piecePickerObserver = Engine.handleEvent(state)
    val draggingModule = Dragging.createModule[Models.Piece]()

    div(
      cls("flex flex-row gap-4 w-full h-full m-4"),
      div("controls"),
      BoardContainer.component(state.boardState),
      PiecePicker.component(piecePickerObserver, draggingModule),
      draggingModule.documentBindings
    )
  }
}
