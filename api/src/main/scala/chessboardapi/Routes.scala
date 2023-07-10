package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.HttpModel._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

object Routes {

  def chessboardRoutes(
      stateRefIo: IO[Ref[IO, ChessboardRepository.State]]
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] {
      case req @ PUT -> Root / "chessboard" => for {
          data <- req.as[CreateChessboard_In]
          stateRef <- stateRefIo
          _ <- ChessboardRepository.append(stateRef, data)
          resp <- Ok()
        } yield (resp)

      case GET -> Root / "chessboard" => for {
          stateRef <- stateRefIo
          entries <- ChessboardRepository.list(stateRef)
          resp <- Ok(entries)
        } yield (resp)
    }
  }

}
