package chessboardapi

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref
import chessboardapi.game.GameModel
import chessboardapi.game.GameRepository

case class GlobalState(gameServiceStateRef: Ref[IO, GameModel.RepositoryState])

object Main extends IOApp.Simple {

  def initState(): IO[GlobalState] = for {
    gssRef <- GameRepository.createState()
    globalState = GlobalState(gssRef)
  } yield (globalState)

  override def run: IO[Unit] = debugRun()

  private def debugRun(): IO[Unit] = for {
    globalState <- initState()
    _ <- Server
      .create(globalState)
      .use { _ =>
        IO.readLine >> IO.unit
      }
  } yield ()

}
