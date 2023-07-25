package example.pages.creator

import com.raquo.laminar.api.L._
import example.AppModel
import example.HttpClient
import example.components.BoardComponent
import example.components.DraggingPiece

object CreatorPage {
  import CreatorPageModel._

  def component(dm: AppModel.DM, httpClient: HttpClient): HtmlElement = {
    val bus = new EventBus[Event]
    val logicModule = CreatorPageLogic.wire(bus.events, httpClient)
    val boardHandler = Observer[BoardComponent.Event] { e =>
      // TODO: should be flatten
      val generalEv: Event = e match {
        case BoardComponent.ElementRefChanged(v) => BoardContainerRefChanged(v)
        case BoardComponent.PieceDragging(e, fromPos) =>
          PlacedPieceDragging(e, fromPos)
      }
      bus.emit(generalEv)
    }
    val deleteZoneHandler = Observer[DeleteZoneComponent.Event] { e =>
      val generalEv: Event = e match {
        case DeleteZoneComponent.RefChanged(v) => RemoveZoneRefChanged(v)
      }
      bus.emit(generalEv)
    }
    val state: State = logicModule.state
    val boardData: BoardComponent.Data = BoardComponent.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.boardSize.signal,
      placedPieces = state.placedPieces.signal,
      dm = dm,
      highlightedTiles = Val(Set()),
      isFlipped = Val(false)
    )
    val piecePickerData = PiecePickerComponent.Data(dm)

    div(
      logicModule.bindings,
      cls("flex flex-row gap-4 m-4"),
      GameSettingsComponent.component(state, bus.writer),
      BoardComponent.create(boardData, boardHandler),
      div(
        cls("flex flex-col w-[200px] justify-between"),
        PiecePickerComponent.create(piecePickerData, bus.writer),
        DeleteZoneComponent.create(deleteZoneHandler)
      ),
      child <-- draggingPieceComponentSignal(state)
    )
  }

  def draggingPieceComponentSignal(state: State): Signal[Node] = {
    val data = DraggingPiece.Data(
      draggingPieceState = state.draggingPieceState.signal,
      boardSize = state.boardSize.signal,
      canvasSize = AppModel.DefaultBoardCanvasSize
    )

    DraggingPiece.componentSignal(data)
  }

}
