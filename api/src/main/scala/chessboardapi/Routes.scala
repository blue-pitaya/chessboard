package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardapi.game.GameRepository
import chessboardapi.game.GameRepositoryModel
import chessboardcore.HttpModel._
import chessboardcore.Model._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2

object Routes {
  def gameRoutes(
      stateRef: Ref[IO, GameRepositoryModel.State],
      ws: WebSocketBuilder2[IO]
  ): HttpRoutes[IO] = {
    val GamePart = "game"
    implicit val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      case req @ PUT -> Root / GamePart => for {
          data <- req.as[CreateGame_In]
          board = data.board
          gameId <- GameRepository.create(stateRef, board)
          resp <- Ok(CreateGame_Out(gameId))
        } yield (resp)

      case _ -> Root / GamePart / gameId / "ws" => for {
          resp <- GameRepository.join(gameId, stateRef, ws)
        } yield (resp)
    }
  }
}
