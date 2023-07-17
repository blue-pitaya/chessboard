package example.pages.game

import cats.effect.IO
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.AppModel
import example.HttpClient
import example.pages.creator.BoardModel
import example.pages.creator.ExBoard
import io.laminext.websocket.WebSocket
import io.laminext.websocket.circe._
import io.circe.generic.auto._

object GamePage2 {
  sealed trait Event
  case class PingClicked() extends Event
  case class LoadBoard() extends Event

  case class State(board: Var[Board])

  private def initialState = State(Var(Board.empty))

  def component(gameId: String, dm: AppModel.DM): Element = {
    val state = initialState
    val bus = new EventBus[Event]
    val ws: WebSocket[WsEv, WsEv] = WebSocket
      .url(HttpClient.gameWebSockerUrl(gameId))
      .json[WsEv, WsEv]
      .build()

    div(
      boardComponent(state, dm),
      ws.connect,
      onMountCallback(ctx => onMounted(bus, ctx.owner, ws, state))
    )
  }

  private def onMounted(
      bus: EventBus[Event],
      owner: Owner,
      ws: WebSocket[WsEv, WsEv],
      state: State
  ): Unit = {
    GameLogic.wireGamePage2(bus.events, state, ws)(owner)
    bus.emit(LoadBoard())
  }

  private def boardComponent(state: State, dm: AppModel.DM): Element = {
    val handler = (event: BoardModel.Event) => IO.unit
    val boardData: BoardModel.Data = BoardModel.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.board.signal.map(_.size),
      placedPieces = state.board.signal.map(pieces),
      dm = dm
    )

    ExBoard.component(boardData, handler)
  }

  private def pieces(board: Board): Map[Vec2d, BoardModel.PieceUiModel] = {
    board
      .pieces
      .map { p =>
        val pos = p.pos
        val pieceUiModel = BoardModel.PieceUiModel(p.piece, Var(true))
        (pos, pieceUiModel)
      }
      .toMap
  }
}
