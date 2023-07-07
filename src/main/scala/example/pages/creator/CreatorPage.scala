package example.pages.creator

import com.raquo.laminar.api.L._
import example.exp.EvHandler
import example.exp.ExApp
import example.exp.ExAppModel
import example.exp.ExBoard
import example.exp.ExBoardForm
import example.exp.ExDraggingPiece

object CreatorPage {
  def component(): HtmlElement = {
    val state = ExAppModel.State.init
    val handler = (e: ExAppModel.Ev) => EvHandler.handle(state, e)

    div(
      cls("flex flex-row gap-4 m-4"),
      ExBoardForm.component(state, handler),
      ExBoard.component(state, handler),
      ExApp.component(state, handler),
      child <-- ExDraggingPiece.componentSignal(state, handler),
      state.dm.documentBindings
    )
  }
}
