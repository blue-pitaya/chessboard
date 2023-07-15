package example.pages.game

import com.raquo.laminar.api.L._
import cats.effect.IO
import chessboardcore.Model
import example.pages.creator.ExBoard
import example.pages.creator.BoardModel
import example.pages.creator.ExAppModel
import chessboardcore.Vec2d

object GamePageModel {
  case class State(
      fetchedGameInfo: Var[Option[Model.GameInfo]],
      pieces: Var[Map[Vec2d, Model.Piece]]
  )
  object State {
    def init = State(fetchedGameInfo = Var(None), pieces = Var(Map()))
  }
}

object GamePage {
  import GamePageModel._

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

  def boardComponent(state: State, gameInfo: Model.GameInfo): Element = {
    val handler = (event: BoardModel.Event) => IO.unit
    val signals: BoardModel.Signals = BoardModel.Signals(
      canvasSize = ExAppModel.DefaultBoardCanvasSize,
      boardSize = Val(gameInfo.board.size),
      placedPieces = ???,
      dm = ???
    )

    ExBoard.component(signals, handler)
  }

  def loadFromServer(id: String, state: State): IO[Unit] = ???
}
