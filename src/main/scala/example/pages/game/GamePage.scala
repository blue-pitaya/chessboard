package example.pages.game

import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.AppModel
import example.components.BoardComponent
import example.components.DraggingPiece
import org.scalajs.dom
import chessboardcore.Model.GameOverState.Draw
import chessboardcore.Model.GameOverState.WinFor

object GamePage {
  sealed trait Event
  case class RequestGameState() extends Event

  case class State(
      gameState: Var[TrueGameState],
      players: Var[Map[PieceColor, PlayerState]],
      gameStarted: Var[Boolean],
      playerId: String,
      draggingPieceState: Var[Option[DraggingPiece.DraggingPieceState]],
      boardComponentRef: Var[Option[dom.Element]],
      pieces: Var[Map[Vec2d, BoardComponent.PieceUiModel]],
      highlightedTiles: Var[Set[Vec2d]],
      msgFromApi: Var[Option[String]]
  )

  def component(gameId: String, dm: AppModel.DM): Element = {
    val bus = new EventBus[Event]
    val plSectionBus = new EventBus[PlayersSectionComponent.Event]
    val boardBus = new EventBus[BoardComponent.Event]

    val gameServiceModule = GameService
      .wire(gameId, bus.events, plSectionBus.events, boardBus.events)
    val state = gameServiceModule.state

    div(
      gameServiceModule.bindings,
      cls("flex flex-col gap-4 m-4"),
      div(
        cls("flex flex-row gap-4"),
        boardComponent(state, dm, boardBus.writer),
        playersSectionComponent(state, plSectionBus.writer)
      ),
      div(child.text <-- gameOverTextSignal(state.gameState.signal)),
      div(child.text <-- state.msgFromApi.signal.map(_.getOrElse(""))),
      child <-- draggingPieceComponentSignal(state),
      onMountCallback { ctx =>
        bus.emit(RequestGameState())
      }
    )
  }

  private def gameOverTextSignal(
      gameState: Signal[TrueGameState]
  ): Signal[String] = {
    gameState.map { s =>
      s.gameOver match {
        case None                        => ""
        case Some(Draw(reason))          => reason
        case Some(WinFor(White, reason)) => s"White won! $reason"
        case Some(WinFor(Black, reason)) => s"Black won! $reason"
      }
    }
  }

  private def boardComponent(
      state: State,
      dm: AppModel.DM,
      handler: Observer[BoardComponent.Event]
  ): Element = {
    val isFlippedSignal = state
      .players
      .signal
      .combineWith(state.gameStarted.signal)
      .map { case (players, gameStarted) =>
        GameService.shouldBoardBeFlipped(state.playerId, players, gameStarted)
      }
    val boardData = BoardComponent.Data(
      canvasSize = AppModel.DefaultBoardCanvasSize,
      boardSize = state.gameState.signal.map(_.board.size),
      placedPieces = state.pieces.signal,
      dm = dm,
      highlightedTiles = state.highlightedTiles.signal,
      isFlipped = isFlippedSignal
    )

    BoardComponent.create(boardData, handler)
  }

  private def playersSectionComponent(
      state: State,
      plSectionObs: Observer[PlayersSectionComponent.Event]
  ): Element = {
    val data = PlayersSectionComponent.Data(
      state.playerId,
      state.players.signal.map(_.get(White)),
      state.players.signal.map(_.get(Black)),
      state.gameStarted.signal,
      state.gameState.signal.map(_.turn)
    )

    PlayersSectionComponent.create(data, plSectionObs)
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
