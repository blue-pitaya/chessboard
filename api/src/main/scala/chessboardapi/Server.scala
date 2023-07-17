package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.middleware._
import org.http4s.server.websocket.WebSocketBuilder2

object Server {

  def create(state: GlobalState): Resource[IO, Unit] = {
    val boardRepoState = ChessboardRepository.createState()

    val finalHttpApp = (ws: WebSocketBuilder2[IO]) => {
      val boardRoutes = Routes.chessboardRoutes(state.boardStateRef)
      val gameRoutes = Routes.gameRoutes(state.gameServiceStateRef, ws)
      val routes = boardRoutes <+> gameRoutes
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

}
