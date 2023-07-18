package chessboardapi

import fs2.Stream
import fs2.Pipe
import org.http4s.websocket.WebSocketFrame
import cats.effect.IO
import fs2.concurrent.Topic
import io.circe.Encoder
import io.circe.Decoder
import io.circe.parser
import io.circe.syntax._

object WebSockerBroadcaster {
  type SendStream = Stream[IO, WebSocketFrame]
  type RecvPipe = Pipe[IO, WebSocketFrame, Unit]

  def create[A](
      handle: A => IO[A]
  )(implicit dec: Decoder[A], enc: Encoder[A]): IO[(SendStream, RecvPipe)] = {
    for {
      topic <- Topic[IO, A]
      recvPipe = (inStream: Stream[IO, WebSocketFrame]) =>
        inStream.evalMap(w => decode(w)).evalMap(handle).through(topic.publish)
      sendStream = topic.subscribeUnbounded.evalMap(w => encode(w))
    } yield ((sendStream, recvPipe))
  }

  private def encode[A](v: A)(implicit enc: Encoder[A]): IO[WebSocketFrame] = {
    val json = v.asJson.toString()
    IO.pure(WebSocketFrame.Text(json))
  }

  private def decode[A](
      frame: WebSocketFrame
  )(implicit dec: Decoder[A]): IO[A] = {
    for {
      frameDataStr <- IO.fromEither(frame.data.decodeUtf8)
      event <- IO.fromEither(parser.decode[A](frameDataStr))
    } yield (event)
  }
}
