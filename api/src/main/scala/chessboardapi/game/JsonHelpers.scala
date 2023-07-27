package chessboardapi.game

import cats.effect.IO
import chessboardcore.HttpModel._
import chessboardcore.Model._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import org.http4s.websocket.WebSocketFrame

object JsonHelpers {
  def encodeValue(v: GameEvent_Out): IO[WebSocketFrame] = {
    val json = v.asJson.toString()
    IO.pure(WebSocketFrame.Text(json))
  }

  def decodeFrame(frame: WebSocketFrame): IO[WsInputMessage] = {
    for {
      frameDataStr <- IO.fromEither(frame.data.decodeUtf8)
      event <- IO.fromEither(parser.decode[WsInputMessage](frameDataStr))
    } yield (event)
  }
}
