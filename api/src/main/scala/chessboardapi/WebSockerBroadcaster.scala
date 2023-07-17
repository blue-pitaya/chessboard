package chessboardapi

import fs2.Stream
import fs2.Pipe
import org.http4s.websocket.WebSocketFrame
import cats.effect.IO
import fs2.concurrent.Topic

object WebSockerBroadcaster {
  type SendStream = Stream[IO, WebSocketFrame]
  type RecvPipe = Pipe[IO, WebSocketFrame, Unit]

  def create[A, B](
      decode: WebSocketFrame => IO[A],
      handle: A => IO[B],
      encode: B => IO[WebSocketFrame]
  ): IO[(SendStream, RecvPipe)] = {
    for {
      topic <- Topic[IO, B]
      recvPipe: RecvPipe = inStream =>
        inStream.evalMap(decode).evalMap(handle).through(topic.publish)
      sendStream: SendStream = topic.subscribeUnbounded.evalMap(encode)
    } yield ((sendStream, recvPipe))
  }
}
