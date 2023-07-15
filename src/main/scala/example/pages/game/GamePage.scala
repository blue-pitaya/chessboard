package example.pages.game

import cats.effect.IO
import chessboardcore.Model
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.Utils
import example.pages.creator.BoardModel
import example.pages.creator.ExBoard

object GamePageModel {
  case class State(
      fetchedGameInfo: Var[Option[Model.GameInfo]],
      pieces: Var[Map[Vec2d, BoardModel.PieceUiModel]]
  )
  object State {
    def init = State(fetchedGameInfo = Var(None), pieces = Var(Map()))
  }
}

object GamePage {
  import GamePageModel._
  import example.AppModel._

  def component(
      id: String,
      dm: DM,
      fetchGameInfo: String => IO[Model.GameInfo]
  ): Element = {
    val state = State.init
    val _boardComponent = (gi: Model.GameInfo) => boardComponent(state, gi, dm)

    div(
      child <-- innerComponentSignal(state, _boardComponent),
      onMountCallback { _ =>
        // TODO: strange signature
        Utils
          .catsRun[String](id => handleGameInfo(fetchGameInfo(id), state))(id)
      }
    )
  }

  def handleGameInfo(gameInfoIo: IO[Model.GameInfo], state: State): IO[Unit] = {
    for {
      gameInfo <- gameInfoIo
      _ <- IO(state.fetchedGameInfo.set(Some(gameInfo)))
      _ <- IO(state.pieces.set(pieces(gameInfo)))
    } yield ()
  }

  def pieces(gameInfo: Model.GameInfo): Map[Vec2d, BoardModel.PieceUiModel] = {
    gameInfo
      .board
      .pieces
      .map { p =>
        val pos = p.pos
        val pieceUiModel = BoardModel.PieceUiModel(p.piece, Var(true))
        (pos, pieceUiModel)
      }
      .toMap
  }

  def innerComponentSignal(
      state: State,
      boardComponent: Model.GameInfo => Element
  ): Signal[Element] = state
    .fetchedGameInfo
    .signal
    .map {
      case Some(v) => loadedComponent(state, v, boardComponent(v))
      case None    => notLoadedComponent()
    }

  def notLoadedComponent(): Element = {
    div("Fetching game from server...")
  }

  def loadedComponent(
      state: State,
      gameInfo: Model.GameInfo,
      boardComponent: Element
  ): Element = {
    div(p("OK!"), boardComponent)
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

}
