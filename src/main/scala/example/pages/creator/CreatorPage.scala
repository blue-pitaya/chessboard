package example.pages.creator

import com.raquo.laminar.api.L._

object CreatorPage {
  def component(): HtmlElement = {
    val state = ExAppModel.State.init
    val handler = (e: ExAppModel.Ev) => EvHandler.handle(state, e)

    div(
      cls("flex flex-row gap-4 m-4"),
      ExBoardForm.component(state, handler),
      ExBoard.component(state, handler),
      div(
        cls("flex flex-col w-[200px] justify-between"),
        ExApp.component(state, handler),
        ExDeleteZone.component(state, handler)
      ),
      child <-- ExDraggingPiece.componentSignal(state, handler),
      state.dm.documentBindings
    )
  }
}
