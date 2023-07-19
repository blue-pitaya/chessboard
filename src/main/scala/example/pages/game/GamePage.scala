package example.pages.game

import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.AppModel
import example.HttpClient
import example.components.BoardComponent
import example.components.DraggingPiece
import io.circe.generic.auto._
import io.laminext.websocket.WebSocket
import io.laminext.websocket.circe._
import org.scalajs.dom

object GamePage {
  sealed trait Event
  case class RequestGameState() extends Event

  case class State(
      gameState: Var[GameState],
      playerId: String,
      draggingPieceState: Var[Option[DraggingPiece.DraggingPieceState]],
      boardComponentRef: Var[Option[dom.Element]],
      pieces: Var[Map[Vec2d, BoardComponent.PieceUiModel]],
      highlightedTiles: Var[Set[Vec2d]]
  )

  private def createState(playerId: String) = State(
    Var(GameState.empty),
    playerId,
    Var(None),
    Var(None),
    Var(Map()),
    Var(Set())
  )

  def component(gameId: String, dm: AppModel.DM): Element = {
    val playerId = chessboardcore.Utils.unsafeCreateId()
    val state = createState(playerId)
    val bus = new EventBus[Event]
    val plSectionBus = new EventBus[PlayersSection.Event]
    val boardBus = new EventBus[BoardComponent.Event]
    val ws: WebSocket[WsEv, WsEv] = WebSocket
      .url(HttpClient.gameWebSockerUrl(gameId))
      .json[WsEv, WsEv]
      .build()

    div(
      GameLogic
        .wire(bus.events, plSectionBus.events, boardBus.events, state, ws),
      cls("flex flex-row gap-4 m-4"),
      boardComponent(state, dm, boardBus.writer),
      playersSectionComponent(state, plSectionBus.writer),
      ws.connect,
      child <-- draggingPieceComponentSignal(state),
      onMountCallback { ctx =>
        bus.emit(RequestGameState())
      }
    )
  }

  private def boardComponent(
      state: State,
      dm: AppModel.DM,
      handler: Observer[BoardComponent.Event]
  ): Element = {
    val boardData = BoardComponent.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.gameState.signal.map(_.board.size),
      placedPieces = state.pieces.signal,
      dm = dm,
      highlightedTiles = state.highlightedTiles.signal
    )

    BoardComponent.create(boardData, handler)
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

  private def draggingPieceComponentSignal(state: State): Signal[Node] = {
    val data = DraggingPiece.Data(
      draggingPieceState = state.draggingPieceState.signal,
      boardSize = state.gameState.signal.map(_.board.size),
      canvasSize = AppModel.DefaultBoardCanvasSize
    )

    DraggingPiece.componentSignal(data)
  }
}
