package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware._

object Server {

  def create(): Resource[IO, Unit] = {
    val boardRepoState = ChessboardRepository.createState()

    val boardRoutes = Routes
      .chessboardRoutes(ChessboardRepository.createState())
    val gameRoutes = Routes.gameRoutes(
      Ref.of[IO, GameServiceModel.State](GameServiceModel.State.init)
    )
    val routes = boardRoutes <+> gameRoutes
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
