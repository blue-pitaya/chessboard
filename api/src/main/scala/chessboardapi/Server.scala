package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Resource
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware._

object Server {

  def create(): Resource[IO, Unit] = {
    val routes = Routes.chessboardRoutes(ChessboardRepository.createState())
    // FIXME: unsafe CORS rule
    val corsService = CORS.policy.withAllowOriginAll(routes).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(corsService)

    for {
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }

}
