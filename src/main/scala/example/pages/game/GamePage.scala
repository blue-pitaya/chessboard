package example.pages.game

import cats.effect.IO
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.AppModel
import example.HttpClient
import example.pages.creator.BoardModel
import example.pages.creator.ExBoard
import io.circe.generic.auto._
import io.laminext.websocket.WebSocket
import io.laminext.websocket.circe._

object GamePage {
  sealed trait Event
  case class RequestGameState() extends Event

  case class State(gameState: Var[GameState], playerId: String)

  private def createState(playerId: String) =
    State(Var(GameState.empty), playerId)

  def component(gameId: String, dm: AppModel.DM): Element = {
    val playerId = chessboardcore.Utils.unsafeCreateId()
    val state = createState(playerId)
    val bus = new EventBus[Event]
    val plSectionBus = new EventBus[PlayersSection.Event]
    val ws: WebSocket[WsEv, WsEv] = WebSocket
      .url(HttpClient.gameWebSockerUrl(gameId))
      .json[WsEv, WsEv]
      .build()

    div(
      cls("flex flex-row gap-4 m-4"),
      boardComponent(state, dm),
      playersSectionComponent(state, plSectionBus.writer),
      ws.connect,
      onMountCallback { ctx =>
        GameLogic
          .wireGamePage2(bus.events, plSectionBus.events, state, ws)(ctx.owner)
        bus.emit(RequestGameState())
      }
    )
  }

  private def boardComponent(state: State, dm: AppModel.DM): Element = {
    val handler = (event: BoardModel.Event) => IO.println(event)
    val boardData = BoardModel.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.gameState.signal.map(_.board.size),
      placedPieces = state.gameState.signal.map(gs => pieces(gs.board)),
      dm = dm
    )

    ExBoard.component(boardData, handler)
  }

  private def playersSectionComponent(
      state: State,
      plSectionObs: Observer[PlayersSection.Event]
  ): Element = {
    val data = PlayersSection.Data(
      state.playerId,
      state.gameState.signal.map(_.whitePlayerState),
      state.gameState.signal.map(_.blackPlayerState),
      state.gameState.signal.map(_.gameStarted),
      state.gameState.signal.map(_.turn)
    )

    PlayersSection.component(data, plSectionObs)
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
