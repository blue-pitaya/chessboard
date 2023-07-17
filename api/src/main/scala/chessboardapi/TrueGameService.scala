package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import io.circe.parser

//give me akka actor vibes ( ͠° ͟ʖ ͡°)
object TrueGameService {
  case class State(board: Board)

  private def initialState = State(Examples.board)

  case class Module(subsrice: WebSocketBuilder2[IO] => IO[Response[IO]])

  def decode(frame: WebSocketFrame): IO[WsEv] = {
    for {
      frameDataStr <- IO.fromEither(frame.data.decodeUtf8)
      event <- IO.fromEither(parser.decode[WsEv](frameDataStr))
    } yield (event)
  }

  def handle(e: WsEv, stateRef: Ref[IO, State]): IO[WsEv] = e match {
    case WsEv(GetBoard()) => for {
        state <- stateRef.get
        board = state.board
      } yield (WsEv(BoardData(board)))
    case _ => IO.pure(WsEv(Ok()))
  }

  def encode(e: WsEv): IO[WebSocketFrame] = {
    val json = e.asJson.toString()
    IO.pure(WebSocketFrame.Text(json))
  }

  def create(): IO[Module] = {
    for {
      stateRef <- Ref.of[IO, State](initialState)
      _handle = (e: WsEv) => handle(e, stateRef)
      x <- WebSockerBroadcaster.create(decode, _handle, encode)
      s = (ws: WebSocketBuilder2[IO]) => {
        ws.build(x._1, x._2)
      }
    } yield (Module(s))
  }

  // case class State(
  //    info: GameInfo,
  //    whitePlayer: PlayerState,
  //    blackPlayer: PlayerState
  // )
  //
  // def initialState: State = State(
  //  info = GameInfo(
  //    board = Board(
  //      Vec2d(6, 6),
  //      List(
  //        PlacedPiece(pos = Vec2d(0, 0), piece = Piece(White, King)),
  //        PlacedPiece(pos = Vec2d(5, 5), piece = Piece(Black, King))
  //      )
  //    ),
  //    timeSettings = TimeSettings(180),
  //    players = Players.init
  //  ),
  //  whitePlayer = PlayerState.Empty,
  //  blackPlayer = PlayerState.Empty
  // )
}
