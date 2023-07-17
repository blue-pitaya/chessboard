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
      decode: WebSocketFrame => A,
      handle: A => B,
      encode: B => WebSocketFrame
  ): IO[(SendStream, RecvPipe)] = {
    for {
      topic <- Topic[IO, B]
      recvPipe: RecvPipe =
        inStream => inStream.map(decode).map(handle).through(topic.publish)
      sendStream: SendStream = topic.subscribeUnbounded.map(encode)
    } yield ((sendStream, recvPipe))
  }
}
