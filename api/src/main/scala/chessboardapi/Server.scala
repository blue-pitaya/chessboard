package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Resource
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware._
import org.http4s.server.websocket.WebSocketBuilder2
import cats.effect.kernel.Ref
import chessboardapi.game.GameRepositoryModel

object Server {

  def create(): IO[Resource[IO, Unit]] = for {
    gameRepositoryStateRef <- Ref
      .of[IO, GameRepositoryModel.State](GameRepositoryModel.State.init)
    serverResource = {
      val finalHttpApp = (ws: WebSocketBuilder2[IO]) => {
        val gameRoutes = Routes.gameRoutes(gameRepositoryStateRef, ws)
        val routes = gameRoutes
        // FIXME: unsafe CORS rule
        val corsService = CORS.policy.withAllowOriginAll(routes).orNotFound

        Logger.httpApp(true, true)(corsService)
      }

      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpWebSocketApp(finalHttpApp)
        .build
        .map(_ => ())
    }
  } yield (serverResource)

}
