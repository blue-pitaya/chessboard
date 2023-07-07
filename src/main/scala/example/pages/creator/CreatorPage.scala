package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d
import example.pages.creator.logic.BoardUiLogic
import example.pages.creator.logic.DraggingId
import example.pages.creator.components.BoardSettingsComponent
import example.exp.ExApp
import example.exp.ExAppModel
import example.exp.EvHandler
import example.exp.ExBoard

object CreatorPage {
  def component(): HtmlElement = {
    val dm = Dragging.createModule[DraggingId]()
    val boardState = BoardUiLogic.State.default(Vec2d(6, 6))
    val boardUiObserver = BoardUiLogic.observer(boardState)

    // ex:
    val _dm = Dragging.createModule[ExAppModel.PieceDraggingId]()
    val state = ExAppModel.State.init
    val handler = (e: ExAppModel.Ev) => EvHandler.handle(state, e)

    div(
      cls("flex flex-row gap-4 m-4"),
      BoardSettingsComponent.component(boardState, boardUiObserver),
      // BoardContainer.component(boardState, boardUiObserver, dm),
      ExBoard.component(state, handler),
      ExApp.component(state, handler),
      // PiecePicker.component(boardUiObserver, dm),
      child <-- DraggingPieceContainer.componentSignal(boardState),
      dm.documentBindings,
      _dm.documentBindings
    )
  }
}
