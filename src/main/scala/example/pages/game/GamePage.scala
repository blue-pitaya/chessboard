package example.pages.game

import cats.effect.IO
import chessboardcore.Model
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.pages.creator.BoardModel
import example.pages.creator.ExAppModel
import example.pages.creator.ExBoard

object GamePageModel {
  case class State(
      fetchedGameInfo: Var[Option[Model.GameInfo]],
      pieces: Var[Map[Vec2d, ExAppModel.PieceUiModel]]
  )
  object State {
    def init = State(fetchedGameInfo = Var(None), pieces = Var(Map()))
  }
}

object GamePage {
  import GamePageModel._
  import example.AppModel._

  def component(id: String): Element = {
    val state = State.init

    div(child <-- innerComponentSignal(state))
  }

  def innerComponentSignal(state: State): Signal[Element] = state
    .fetchedGameInfo
    .signal
    .map {
      case Some(s) => loadedComponent(state, s)
      case None    => notLoadedComponent()
    }

  def notLoadedComponent(): Element = {
    div("Fetching game from server...")
  }

  def loadedComponent(state: State, serverState: Model.GameInfo): Element = {
    div(p(state.toString()))
  }

  def boardComponent(
      state: State,
      gameInfo: Model.GameInfo,
      dm: DM
  ): Element = {
    val handler = (event: BoardModel.Event) => IO.unit
    val boardData: BoardModel.Data = BoardModel.Data(
      canvasSize = DefaultBoardCanvasSize,
      boardSize = Val(gameInfo.board.size),
      placedPieces = state.pieces.signal,
      dm = dm
    )

    ExBoard.component(boardData, handler)
  }

  def loadFromServer(id: String, state: State): IO[Unit] = ???
}
