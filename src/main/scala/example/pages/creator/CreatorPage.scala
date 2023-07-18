package example.pages.creator

import com.raquo.laminar.api.L._
import example.pages.creator.BoardModel.ElementRefChanged
import example.pages.creator.BoardModel.PieceDragging
import example.AppModel

object CreatorPage {
  def component(dm: AppModel.DM): HtmlElement = {
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
    val boardData: BoardModel.Data = BoardModel.Data(
      canvasSize = state.canvasSize,
      boardSize = state.boardSize.signal,
      placedPieces = state.placedPieces.signal,
      dm = dm
    )
    val piecePickerData = PiecePickerModel.Data(dm)

    div(
      cls("flex flex-row gap-4 m-4"),
      ExBoardForm.component(state, handler),
      ExBoard.component(boardData, boardHandler),
      div(
        cls("flex flex-col w-[200px] justify-between"),
        PiecePicker.component(piecePickerData, handler),
        DeleteZone.component(handler)
      ),
      child <-- draggingPieceComponentSignal(state)
    )
  }

  def draggingPieceComponentSignal(state: ExAppModel.State): Signal[Node] = {
    val data = ExDraggingPiece.Data(
      draggingPieceState = state.draggingPieceState.signal,
      boardSize = state.boardSize.signal,
      canvasSize = state.canvasSize
    )

    ExDraggingPiece.componentSignal(data)
  }

}
