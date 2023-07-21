package example.pages.game

import chessboardcore.HttpModel._
import chessboardcore.Model._
import chessboardcore.Vec2d
import chessboardcore.gamelogic.MoveLogic
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind
import dev.bluepitaya.laminardragging.Dragging
import example.AppModel
import example.Misc
import example.components.BoardComponent
import example.components.BoardComponent.ElementRefChanged
import example.components.BoardComponent.PieceDragging
import example.components.DraggingPiece
import example.pages.creator.EvHandler
import io.laminext.websocket.WebSocket

object GameLogic {
  case class Module(sendWsEventObserver: Observer[GameEvent_In])

  def wire(
      events: EventStream[GamePage.Event],
      plSectionEvents: EventStream[PlayersSection.Event],
      boardCompEvents: EventStream[BoardComponent.Event],
      state: GamePage.State,
      ws: WebSocket[GameEvent_Out, GameEvent_In]
  ): Seq[Binder.Base] = Seq(
    ws.received.-->(Observer[GameEvent_Out](e => handleWsEvent(e, state))),
    events
      .-->(Observer[GamePage.Event](e => handleEvent(e, state, ws.sendOne))),
    plSectionEvents.-->(
      Observer[PlayersSection.Event](e =>
        handlePlSectionEvent(e, state, ws.sendOne)
      )
    ),
    boardCompEvents.-->(
      Observer[BoardComponent.Event](e =>
        handleBoardComponentEvent(e, state, ws.sendOne)
      )
    )
  )

  private def handleWsEvent(e: GameEvent_Out, state: GamePage.State): Unit =
    e match {
      case GameStateData(v) =>
        state.gameState.set(v)
        state.pieces.set(pieces(v.board))
      case _ => ()
    }

  private def pieces(board: Board): Map[Vec2d, BoardComponent.PieceUiModel] =
    board
      .pieces
      .map { case (pos, piece) =>
        (pos, createPieceModel(piece))
      }

  private def createPieceModel(piece: Piece): BoardComponent.PieceUiModel =
    BoardComponent.PieceUiModel(piece, Var(true))

  private def handleEvent(
      e: GamePage.Event,
      state: GamePage.State,
      sendWsEvent: GameEvent_In => Unit
  ): Unit = e match {
    case GamePage.RequestGameState() => sendWsEvent(GetGameState())
  }

  private def handlePlSectionEvent(
      e: PlayersSection.Event,
      state: GamePage.State,
      sendWsEvent: GameEvent_In => Unit
  ): Unit = e match {
    case PlayersSection.PlayerSit(color) =>
      sendWsEvent(PlayerSit(state.playerId, color))
    case PlayersSection.PlayerReady(color) =>
      sendWsEvent(PlayerReady(state.playerId, color))
  }

  private def handleBoardComponentEvent(
      e: BoardComponent.Event,
      state: GamePage.State,
      sendWsEvent: GameEvent_In => Unit
  ): Unit = {
    e match {
      case ElementRefChanged(v) => state.boardComponentRef.set(Some(v))
      case PieceDragging(e, fromPos) =>
        val pieceOpt = state.pieces.now().get(fromPos)
        val gameStarted = state.gameState.now().gameStarted
        val myPlayerId = state.playerId
        val currentTurn = state.gameState.now().turn
        val isMyPiece = (col: PieceColor) =>
          playerId(state, col).map(_ == myPlayerId).getOrElse(false)
        val sendMoveEvent =
          (toPos: Vec2d) => sendWsEvent(Move(myPlayerId, fromPos, toPos))

        (pieceOpt, gameStarted) match {
          case (Some(piece), true) => if (isMyPiece(piece.piece.color)) {
              val _movePiece =
                (to: Vec2d) => movePiece(state, fromPos, to, piece)
              handlePieceDragging(
                e,
                fromPos,
                piece,
                state,
                sendMoveEvent,
                _movePiece
              )
            } else ()
          case _ => ()
        }
    }
  }

  // TODO: dup?
  private def playerId(
      state: GamePage.State,
      color: PieceColor
  ): Option[String] = state.gameState.now().players.get(color).map(_.id)

  private def handlePieceDragging(
      e: Dragging.Event,
      pos: Vec2d,
      pieceModel: BoardComponent.PieceUiModel,
      state: GamePage.State,
      sendMoveEvent: Vec2d => Unit,
      movePiece: Vec2d => Unit
  ): Unit = {
    lazy val board = state.gameState.now().board
    val _updatePieceDraggingState = () =>
      updatePieceDraggingState(state, e, Misc.pieceImgPath(pieceModel.piece))

    e.kind match {
      case DragEventKind.Start =>
        pieceModel.isVisible.set(false)
        _updatePieceDraggingState()
        val possibleMoves = MoveLogic.possibleMoves(board, pos)
        state.highlightedTiles.set(possibleMoves.toSet)
      case DragEventKind.Move => _updatePieceDraggingState()
      case DragEventKind.End =>
        state.highlightedTiles.set(Set())
        pieceModel.isVisible.set(true)
        state.draggingPieceState.set(None)
        val toPosOpt = tileLogicPos(state, e)
        toPosOpt.foreach { toPos =>
          movePiece(toPos)
          sendMoveEvent(toPos)
        }
    }
  }

  // TODO: dup with EvHandler
  private def movePiece(
      state: GamePage.State,
      fromPos: Vec2d,
      toPos: Vec2d,
      pieceUiModel: BoardComponent.PieceUiModel
  ): Unit = {
    state.pieces.update(v => v.removed(fromPos))
    state.pieces.update(v => v.updated(toPos, pieceUiModel))
  }

  // TODO: dup with EvHandler
  private def tileLogicPos(
      state: GamePage.State,
      e: Dragging.Event
  ): Option[Vec2d] = {
    val boardSize = state.gameState.now().board.size
    val elementRefOpt = state.boardComponentRef.now()

    elementRefOpt.flatMap { elementRef =>
      val canvasSize = AppModel.DefaultBoardCanvasSize
      val canvasPos = EvHandler.getRelativePosition(e.e, elementRef)
      EvHandler.tileLogicPos(boardSize, canvasSize, canvasPos)
    }
  }

  private def updatePieceDraggingState(
      state: GamePage.State,
      draggingEvent: Dragging.Event,
      imgPath: String
  ): Unit = {
    val dps = DraggingPiece.DraggingPieceState(imgPath, draggingEvent)

    state.draggingPieceState.set(Some(dps))
  }

}
