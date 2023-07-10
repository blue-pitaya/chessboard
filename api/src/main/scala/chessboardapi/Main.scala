package chessboardapi

import cats.effect.IOApp
import cats.effect.IO

object Main extends IOApp.Simple {

  override def run: IO[Unit] = Server.run()

}
