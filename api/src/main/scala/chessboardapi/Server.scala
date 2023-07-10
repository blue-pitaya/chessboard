package chessboardapi

import cats.effect.IO
import com.comcast.ip4s._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Server {
  def run(): IO[Unit] = {
    for {
      client <- EmberClientBuilder.default[IO].build
      httpApp = (Routes.chessboardRoutes()).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever
}
