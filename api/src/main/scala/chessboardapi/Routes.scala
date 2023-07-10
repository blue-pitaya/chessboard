package chessboardapi

import org.http4s.HttpRoutes
import cats.effect.IO
import org.http4s.dsl.Http4sDsl

object Routes {
  def chessboardRoutes(): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { case req @ PUT -> Root / "chessboard" =>
      for {
        resp <- Ok(req.as[String])
      } yield resp
    }
  }
}
