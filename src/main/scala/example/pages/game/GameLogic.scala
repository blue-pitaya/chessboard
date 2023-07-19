package example.pages.game

import chessboardcore.Model
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind
import dev.bluepitaya.laminardragging.Dragging
import example.components.BoardComponent
import example.components.BoardComponent.ElementRefChanged
import example.components.BoardComponent.PieceDragging
import example.pages.game.PlayersSection.PlayerReady
import io.laminext.websocket.WebSocket
import example.components.DraggingPiece
import example.Misc
import chessboardcore.Model.PlayerState.Empty
import chessboardcore.Model.PlayerState.Ready
import chessboardcore.Model.PlayerState.Sitting
import example.pages.creator.EvHandler
import example.AppModel
import chessboardcore.gamelogic.MoveLogic

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEv])

  def wire(
      events: EventStream[GamePage.Event],
      plSectionEvents: EventStream[PlayersSection.Event],
      boardCompEvents: EventStream[BoardComponent.Event],
      state: GamePage.State,
      ws: WebSocket[WsEv, WsEv]
  ): Seq[Binder.Base] = Seq(
    ws.received.-->(Observer[Model.WsEv](e => handleWsEvent(e, state))),
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

  private def handleWsEvent(e: WsEv, state: GamePage.State): Unit = e match {
    case WsEv(GameStateData(v)) =>
      state.gameState.set(v)
      state.pieces.set(pieces(v.board))
    case _ => ()
  }

  private def pieces(board: Board): Map[Vec2d, BoardComponent.PieceUiModel] =
    board.pieces.map(p => (p.pos, createPieceModel(p.piece))).toMap

  private def createPieceModel(piece: Piece): BoardComponent.PieceUiModel =
    BoardComponent.PieceUiModel(piece, Var(true))

  private def handleEvent(
      e: GamePage.Event,
      state: GamePage.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case GamePage.RequestGameState() => sendWsEvent(WsEv(GetGameState()))
  }

  private def handlePlSectionEvent(
      e: PlayersSection.Event,
      state: GamePage.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case PlayersSection.PlayerSit(color) =>
      sendWsEvent(WsEv(PlayerSit(state.playerId, color)))
    case PlayerReady() => sendWsEvent(WsEv(Model.PlayerReady(state.playerId)))
  }

  private def handleBoardComponentEvent(
      e: BoardComponent.Event,
      state: GamePage.State,
      sendWsEvent: WsEv => Unit
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
          (toPos: Vec2d) => sendWsEvent(WsEv(Move(myPlayerId, fromPos, toPos)))

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
  ): Option[String] = {
    val plState = color match {
      case Black => state.gameState.now().blackPlayerState
      case White => state.gameState.now().whitePlayerState
    }

    plState match {
      case Empty             => None
      case Ready(playerId)   => Some(playerId)
      case Sitting(playerId) => Some(playerId)
    }
  }

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
