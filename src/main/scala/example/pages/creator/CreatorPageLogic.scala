package example.pages.creator

import cats.effect.IO
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import dev.bluepitaya.laminardragging.Dragging
import example.AppModel
import example.AppRouter
import example.HttpClient
import example.PageKey
import example.Utils
import example.components.BoardComponent
import example.components.DraggingPiece.DraggingPieceState
import example.components.Logic

object CreatorPageLogic {
  import CreatorPageModel._

  case class Module(state: State, bindings: Seq[Binder.Base])

  def wire(events: EventStream[Event], httpClient: HttpClient): Module = {
    val state = createState()
    val bindings =
      Seq(events --> Observer[Event](e => handleEvent(e, state, httpClient)))

    Module(state, bindings)
  }

  private def createState() = State(
    draggingPieceState = Var(None),
    boardContainerRef = Var(None),
    boardSize = Var(Vec2d(6, 6)),
    placedPieces = Var(Map()),
    removeZoneComponentRef = Var(None)
  )

  private def handleEvent(e: Event, s: State, httpClient: HttpClient): Unit = {
    e match {
      case BoardContainerRefChanged(v) => s.boardContainerRef.set(Some(v))

      case BoardHeightChanged(v) => s.boardSize.update(size => Vec2d(size.x, v))

      case BoardWidthChanged(v) => s.boardSize.update(size => Vec2d(v, size.y))

      case PlacedPieceDragging(e, fromPos) =>
        handlePlacedPieceDragging(e, fromPos, s)

      case CreateGameUsingCurrentBoardRequested() => Utils
          .run(createGame(s, httpClient))

      case PickerPieceDragging(e, piece) =>
        handlePickerPieceDragging(e, piece, s)

      case RemoveZoneRefChanged(v) => s.removeZoneComponentRef.set(Some(v))
    }
  }

  private def handlePickerPieceDragging(
      e: Dragging.Event,
      piece: Piece,
      s: State
  ): Unit = {
    e.kind match {
      case Start => updateDraggingPieceState(e, piece, s)
      case Move  => updateDraggingPieceState(e, piece, s)
      case End =>
        s.draggingPieceState.set(None)
        tileLogicPos(e, s).foreach(tilePos => placePiece(tilePos, piece, s))
    }
  }

  private def tileLogicPos(
      draggingEvent: Dragging.Event,
      s: State
  ): Option[Vec2d] = for {
    boardRef <- s.boardContainerRef.now()
    canvasPos = Utils.getRelativePosition(draggingEvent.e, boardRef)
    tilePos <- Logic.tileLogicPos(
      s.boardSize.now(),
      AppModel.DefaultBoardCanvasSize,
      canvasPos,
      false
    )
  } yield (tilePos)

  private def updateDraggingPieceState(
      e: Dragging.Event,
      piece: Piece,
      s: State
  ): Unit = s
    .draggingPieceState
    .set(
      Some(
        DraggingPieceState(
          imgPath = Utils.pieceImgPath(piece),
          draggingEvent = e
        )
      )
    )

  private def createGame(state: State, httpClient: HttpClient): IO[Unit] = for {
    resp <- httpClient.createGame(board(state))
    _ <- IO(AppRouter.router.pushState(PageKey.Game(resp.id)))
  } yield ()

  private def board(state: State): Board = {
    val boardSize = state.boardSize.now()
    val piecesOnBoard = state
      .placedPieces
      .now()
      .collect {
        case (pos, pieceUiModel) if Logic.isPosOnBoard(pos, boardSize) =>
          (pos, pieceUiModel.piece)
      }

    Board(size = boardSize, pieces = piecesOnBoard)
  }

  private def handlePlacedPieceDragging(
      e: Dragging.Event,
      fromPos: Vec2d,
      state: State
  ): Unit = state
    .placedPieces
    .now()
    .get(fromPos)
    .foreach { pieceModel =>
      e.kind match {
        case Start =>
          pieceModel.isVisible.set(false)
          updateDraggingPieceState(e, pieceModel.piece, state)
        case Move => updateDraggingPieceState(e, pieceModel.piece, state)
        case End  => onEndPlacedPieceDragging(e, fromPos, pieceModel, state)
      }
    }

  private def onEndPlacedPieceDragging(
      e: Dragging.Event,
      fromPos: Vec2d,
      piece: BoardComponent.PieceUiModel,
      state: State
  ): Unit = {
    piece.isVisible.set(true)
    state.draggingPieceState.set(None)
    tileLogicPos(e, state)
      .foreach(tilePos => movePlacedPiece(state, fromPos, tilePos, piece))

    if (isOverRemoveZone(state, e)) {
      removePiece(fromPos, state)
    }
  }

  private def isOverRemoveZone(state: State, event: Dragging.Event): Boolean =
    state
      .removeZoneComponentRef
      .now()
      .map(ref => Utils.isPointerInsideElement(event.e, ref))
      .getOrElse(false)

  private def movePlacedPiece(
      state: State,
      fromPos: Vec2d,
      toPos: Vec2d,
      pieceUiModel: BoardComponent.PieceUiModel
  ): Unit = {
    removePiece(fromPos, state)
    placePiece(toPos, pieceUiModel.piece, state)
  }

  private def removePiece(pos: Vec2d, state: State): Unit = state
    .placedPieces
    .update(v => v.removed(pos))

  private def placePiece(pos: Vec2d, piece: Piece, state: State): Unit = state
    .placedPieces
    .update(v => v.updated(pos, BoardComponent.PieceUiModel(piece, Var(true))))
}
