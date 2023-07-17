package example.pages.game

import cats.effect.IO
import cats.effect.kernel.Resource
import chessboardcore.Model
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.Utils
import example.pages.creator.BoardModel
import example.pages.creator.ExBoard
import org.http4s.client.websocket.WSConnectionHighLevel

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

  type AuthToken = String
  type GameId = String
  type WS = Resource[IO, WSConnectionHighLevel[IO]]

  def component(
      dm: DM,
      fetchGameInfo: IO[Model.GameInfo],
      ws: Resource[IO, WSConnectionHighLevel[IO]]
  ): Element = {
    val state = State.init
    val wsEventBus = new EventBus[Model.WsEvent]

    val _boardComponent = (gi: Model.GameInfo) => boardComponent(state, gi, dm)
    val _handleGameInfo = (gi: Model.GameInfo) => handleGameInfo(gi, state)
    val _onMounted =
      onMounted(fetchGameInfo, _handleGameInfo, useWs(ws, wsEventBus.events))
    val _loadedComponent = (gi: Model.GameInfo) =>
      loadedComponent(state, gi, _boardComponent(gi), ???)

    div(
      child <-- innerComponentSignal(state, _loadedComponent),
      onMountCallback(_ => Utils.run(_onMounted))
    )
  }

  def onMounted(
      fetchGameInfo: IO[Model.GameInfo],
      handleGameInfo: Model.GameInfo => IO[Unit],
      useWs: IO[Unit]
  ): IO[Unit] = for {
    gameInfo <- fetchGameInfo
    _ <- handleGameInfo(gameInfo)
    _ <- useWs
  } yield ()

  def useWs(ws: WS, wsEvents: EventStream[Model.WsEvent]): IO[Unit] = ws
    .use { conn =>
      val receiveMessages: IO[Unit] = conn
        .receiveStream
        .evalTap(frame => IO.println(frame.toString()))
        .compile
        .drain

      receiveMessages
    }

  // def authToken(): IO[String] = for {
  //  tokenOpt <- tokenFromLocalStorage()
  //  token <- tokenOpt match {
  //    case None        => generateAndSaveToken()
  //    case Some(value) => IO.pure(value)
  //  }
  // } yield (token)

  // private val AuthTokenKey = "auth_token"

  // def tokenFromLocalStorage(): IO[Option[String]] =
  //  IO(Option(dom.window.localStorage.getItem("auth_token")))

  // def generateAndSaveToken(): IO[String] = for {
  //  token <- chessboardcore.Utils.createId()
  //  _ <- IO(dom.window.localStorage.setItem("auth_token", token))
  // } yield (token)

  def handleGameInfo(gameInfo: Model.GameInfo, state: State): IO[Unit] = {
    for {
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
      loadedComponent: Model.GameInfo => Element
  ): Signal[Element] = state
    .fetchedGameInfo
    .signal
    .map {
      case Some(v) => loadedComponent(v)
      case None    => notLoadedComponent()
    }

  def notLoadedComponent(): Element = {
    div("Fetching game from server...")
  }

  def loadedComponent(
      state: State,
      gameInfo: Model.GameInfo,
      boardComponent: Element,
      pingComponent: Element
  ): Element = {
    div(cls("flex flex-row gap-4 p-4"), boardComponent, pingElement)
  }

  def pingElement(): Element = {
    button(
      "ping",
      onClick.mapToUnit -->
        Observer[Unit] { _ =>
          ???
        }
    )
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
