package chessboardapi

import cats.effect.IOApp
import cats.effect.IO

object Main extends IOApp.Simple {

  override def run: IO[Unit] = debugRun()

  private def debugRun(): IO[Unit] = Server
    .create()
    .use { _ =>
      IO.readLine >> IO.unit
    }

  private def prodRun() = Server.create().useForever

}
