package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Vec2d
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

//give me akka actor vibes ( ͠° ͟ʖ ͡°)
object TrueGameService {
  case class State(
      info: GameInfo,
      whitePlayer: PlayerState,
      blackPlayer: PlayerState
  )

  case class Module(subsrice: WebSocketBuilder2[IO] => IO[Response[IO]])

  def decode(frame: WebSocketFrame): WsEvent = MPing(frame.toString())

  def handle(e: WsEvent): WsEvent = e match {
    case MPing(v) => MPong(v)
    case MPong(v) => ???
  }

  def encode(e: WsEvent): WebSocketFrame = {
    val json = e.asJson.toString()
    WebSocketFrame.Text(json)
  }

  def create(): IO[Module] = {
    for {
      stateRef <- Ref.of[IO, State](initialState)
      x <- WebSockerBroadcaster.create(decode, handle, encode)
      s = (ws: WebSocketBuilder2[IO]) => {
        ws.build(x._1, x._2)
      }
    } yield (Module(s))
  }

  def handleInput(e: WsEvent, sr: Ref[IO, State]): IO[WsEvent] = e match {
    case MPing(v) => for {
        state <- sr.get
      } yield (MPong(state.toString()))
    case MPong(v) => ???
  }

  def initialState: State = State(
    info = GameInfo(
      board = Board(
        Vec2d(6, 6),
        List(
          PlacedPiece(pos = Vec2d(0, 0), piece = Piece(White, King)),
          PlacedPiece(pos = Vec2d(5, 5), piece = Piece(Black, King))
        )
      ),
      timeSettings = TimeSettings(180),
      players = Players.init
    ),
    whitePlayer = PlayerState.Empty,
    blackPlayer = PlayerState.Empty
  )
}
