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
import chessboardcore.Model._

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
      state2Ref: Ref[IO, GameServiceModel.State],
      ws: WebSocketBuilder2[IO]
  ): HttpRoutes[IO] = {
    val GamePart = "game"
    implicit val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      case req @ PUT -> Root / GamePart => for {
          data <- req.as[CreateGame_In]
          board = data.board
          gameId <- GameService.create(state2Ref, board)
          resp <- Ok(CreateGame_Out(gameId))
        } yield (resp)

      case _ -> Root / GamePart / gameId / "ws" => for {
          resp <- GameService.join(gameId, state2Ref, ws)
        } yield (resp)
    }
  }
}
