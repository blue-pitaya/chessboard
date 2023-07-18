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

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEv])

  def wireGamePage2(
      events: EventStream[GamePage.Event],
      plSectionEvents: EventStream[PlayersSection.Event],
      boardCompEvents: EventStream[BoardComponent.Event],
      state: GamePage.State,
      ws: WebSocket[WsEv, WsEv]
  )(implicit owner: Owner): Unit = {
    ws.received.addObserver(Observer[Model.WsEv](e => handleWsEvent(e, state)))
    events.addObserver(
      Observer[GamePage.Event](e => handleEvent(e, state, ws.sendOne))
    )
    plSectionEvents.addObserver(
      Observer[PlayersSection.Event](e =>
        handlePlSectionEvent(e, state, ws.sendOne)
      )
    )
    boardCompEvents.addObserver(
      Observer[BoardComponent.Event](e => handleBoardComponentEvent(e, state))
    )
  }

  private def handleWsEvent(e: WsEv, state: GamePage.State): Unit = e match {
    case WsEv(GameStateData(v)) =>
      state.gameState.set(v)
      state.pieces.set(pieces(v.board))
    case _ => ()
  }

  private def pieces(board: Board): Map[Vec2d, BoardComponent.PieceUiModel] = {
    board
      .pieces
      .map { p =>
        val pos = p.pos
        val pieceUiModel = BoardComponent.PieceUiModel(p.piece, Var(true))
        (pos, pieceUiModel)
      }
      .toMap
  }

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
      state: GamePage.State
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

        (pieceOpt, gameStarted) match {
          case (Some(piece), true) => if (isMyPiece(piece.piece.color))
              handlePieceDragging(e, piece, state)
            else ()
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
      pieceModel: BoardComponent.PieceUiModel,
      state: GamePage.State
  ): Unit = {
    val _updatePieceDraggingState = () =>
      updatePieceDraggingState(state, e, Misc.pieceImgPath(pieceModel.piece))

    e.kind match {
      case DragEventKind.Start =>
        pieceModel.isVisible.set(false)
        _updatePieceDraggingState()
      case DragEventKind.Move => _updatePieceDraggingState()
      case DragEventKind.End =>
        pieceModel.isVisible.set(true)
        state.draggingPieceState.set(None)
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
