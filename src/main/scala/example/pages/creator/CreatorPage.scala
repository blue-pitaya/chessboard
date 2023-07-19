package example.pages.creator

import com.raquo.laminar.api.L._
import example.AppModel
import example.components.DraggingPiece
import example.components.BoardComponent
import example.Utils

object CreatorPage {
  def component(dm: AppModel.DM): HtmlElement = {
    val state = ExAppModel.State.init
    val handler = (e: ExAppModel.Ev) => EvHandler.handle(state, e)
    val boardHandler = Observer[BoardComponent.Event] { e =>
      val generalEv: ExAppModel.Ev = e match {
        case BoardComponent.ElementRefChanged(v) =>
          ExAppModel.BoardContainerRefChanged(v)
        case BoardComponent.PieceDragging(e, fromPos) =>
          ExAppModel.PlacedPieceDragging(e, fromPos)
      }

      Utils.run(EvHandler.handle(state, generalEv))
    }
    val deleteZoneHandler = Observer[DeleteZoneComponent.Event] { e =>
      val generalEv: ExAppModel.Ev = e match {
        case DeleteZoneComponent.RefChanged(v) =>
          ExAppModel.RemoveZoneRefChanged(v)
      }

      Utils.run(EvHandler.handle(state, generalEv))
    }
    val boardData: BoardComponent.Data = BoardComponent.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.boardSize.signal,
      placedPieces = state.placedPieces.signal,
      dm = dm,
      highlightedTiles = Val(Set())
    )
    val piecePickerData = PiecePickerModel.Data(dm)

    div(
      cls("flex flex-row gap-4 m-4"),
      ExBoardForm.component(state, handler),
      BoardComponent.create(boardData, boardHandler),
      div(
        cls("flex flex-col w-[200px] justify-between"),
        PiecePicker.component(piecePickerData, handler),
        DeleteZoneComponent.create(deleteZoneHandler)
      ),
      child <-- draggingPieceComponentSignal(state)
    )
  }

  def draggingPieceComponentSignal(state: ExAppModel.State): Signal[Node] = {
    val data = DraggingPiece.Data(
      draggingPieceState = state.draggingPieceState.signal,
      boardSize = state.boardSize.signal,
      canvasSize = AppModel.DefaultBoardCanvasSize
    )

    DraggingPiece.componentSignal(data)
  }

}
