package example.pages.creator

import com.raquo.laminar.api.L._
import example.pages.creator.BoardModel.ElementRefChanged
import example.pages.creator.BoardModel.PieceDragging

object CreatorPage {
  def component(): HtmlElement = {
    val state = ExAppModel.State.init
    val handler = (e: ExAppModel.Ev) => EvHandler.handle(state, e)
    val boardHandler = (e: BoardModel.Event) => {
      val generalEv: ExAppModel.Ev = e match {
        case ElementRefChanged(v) => ExAppModel.BoardContainerRefChanged(v)
        case PieceDragging(e, fromPos) =>
          ExAppModel.PlacedPieceDragging(e, fromPos)
      }
      EvHandler.handle(state, generalEv)
    }

    div(
      cls("flex flex-row gap-4 m-4"),
      ExBoardForm.component(state, handler),
      ExBoard.component(
        BoardModel.Signals.fromCreatorPageState(state),
        boardHandler
      ),
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
