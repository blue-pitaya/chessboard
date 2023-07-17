package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model.PlayerState.Empty
import chessboardcore.Model._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import monocle.AppliedLens
import monocle.syntax.all._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

object TrueGameService {
  case class State(gameState: GameState)
  case class Module(subsrice: WebSocketBuilder2[IO] => IO[Response[IO]])

  private def createState(board: Board): IO[Ref[IO, State]] = Ref.of[IO, State](
    State(GameState(board, PlayerState.Empty, PlayerState.Empty))
  )

  // TODO: move it to websocket function
  private def decode(frame: WebSocketFrame): IO[WsEv] = {
    for {
      frameDataStr <- IO.fromEither(frame.data.decodeUtf8)
      event <- IO.fromEither(parser.decode[WsEv](frameDataStr))
    } yield (event)
  }

  private def handle(e: WsEv, stateRef: Ref[IO, State]): IO[WsEv] = e.e match {
    case GetGameState() => for {
        state <- stateRef.get
      } yield (WsEv(GameStateData(state.gameState)))

    case PlayerSit(color) => for {
        nextState <- stateRef.updateAndGet(s => sitPlayer(color, s))
      } yield (WsEv(GameStateData(nextState.gameState)))

    case _ => IO.pure(WsEv(Ok()))
  }

  private def sitPlayer(color: PieceColor, state: State): State = {
    val playerLens = playerByColor(color, state)

    playerLens.get match {
      case Empty => playerLens.replace(PlayerState.Sitting)
      case _     => state
    }
  }

  private def playerByColor(
      color: PieceColor,
      state: State
  ): AppliedLens[State, PlayerState] = {
    color match {
      case Black => state.focus(_.gameState.blackPlayerState)
      case White => state.focus(_.gameState.whitePlayerState)
    }
  }

  private def encode(e: WsEv): IO[WebSocketFrame] = {
    val json = e.asJson.toString()
    IO.pure(WebSocketFrame.Text(json))
  }

  def create(board: Board): IO[Module] = {
    for {
      stateRef <- createState(board)
      _handle = (e: WsEv) => handle(e, stateRef)
      x <- WebSockerBroadcaster.create(decode, _handle, encode)
      s = (ws: WebSocketBuilder2[IO]) => {
        ws.build(x._1, x._2)
      }
    } yield (Module(s))
  }
}
