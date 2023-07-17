package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.HttpModel._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2

object Routes {

  def chessboardRoutes(
      stateRef: Ref[IO, ChessboardRepository.State]
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case req @ PUT -> Root / "chessboard" => for {
          data <- req.as[CreateChessboard_In]
          _ <- ChessboardRepository.append(stateRef, data)
          resp <- Ok()
        } yield (resp)

      case GET -> Root / "chessboard" => for {
          entries <- ChessboardRepository.list(stateRef)
          resp <- Ok(entries)
        } yield (resp)

    }
  }

  def gameRoutes(
      stateRef: Ref[IO, GameServiceModel.State],
      state2Ref: Ref[IO, GameServiceModel.State2],
      ws: WebSocketBuilder2[IO]
  ): HttpRoutes[IO] = {
    val GamePart = "game"
    implicit val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      case req @ PUT -> Root / GamePart => for {
          data <- req.as[CreateGame_In]
          board = data.board
          gameId <- GameService.create(stateRef, board)
          resp <- Ok(gameId)
        } yield (resp)

      case GET -> Root / GamePart / gameId => for {
          entryOpt <- GameService.get(stateRef, gameId)
          resp <- entryOpt match {
            case None    => NotFound()
            case Some(v) => Ok(v.game)
          }
        } yield (resp)

      case _ -> Root / GamePart / gameId / "ws" => for {
          resp <- GameService.join(gameId, state2Ref, ws)
        } yield (resp)
    }
  }

  // def joinGameRoom(
  //    authToken: String,
  //    gameId: String,
  //    stateRef: Ref[IO, GameServiceModel.State]
  // )(implicit dsl: Http4sDsl[IO]): IO[Response[IO]] = {
  //  import dsl._

  //  for {
  //    gameInfoOpt <- GameService.join(gameId, authToken, stateRef)
  //    resp <- gameInfoOpt match {
  //      case None    => NotFound()
  //      case Some(v) => Ok(v)
  //    }
  //  } yield (resp)
  // }

  // TODO: https://http4s.org/v0.23/docs/auth.html
  // def authorize[A](req: Request[IO], f: String => IO[Response[IO]])(implicit
  //    dsl: Http4sDsl[IO]
  // ): IO[Response[IO]] = {
  //  import dsl._

  //  for {
  //    authTokenHeaderOpt <- IO.pure(req.headers.get(ci"X-Auth-Token"))
  //    resp <- authTokenHeaderOpt match {
  //      case None => Forbidden()
  //      case Some(authHeaders) =>
  //        val token = authHeaders.head.value
  //        f(token)
  //    }
  //  } yield (resp)
  // }
}
