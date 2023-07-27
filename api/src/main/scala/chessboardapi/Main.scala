package chessboardapi

import cats.effect.IO
import cats.effect.IOApp

object Main extends IOApp.Simple {

  override def run: IO[Unit] = for {
    serverResource <- Server.create()
    _ <- serverResource.useForever
  } yield ()

}
