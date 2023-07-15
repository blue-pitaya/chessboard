package chessboardapi

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref

case class GlobalState(
    boardStateRef: Ref[IO, ChessboardRepository.State],
    gameServiceStateRef: Ref[IO, GameServiceModel.State]
)

object Main extends IOApp.Simple {

  def initState(): IO[GlobalState] = for {
    boardStateRef <- ChessboardRepository.createState()
    gameServiceStateRef <- Ref
      .of[IO, GameServiceModel.State](GameServiceModel.State.initDebug)
    globalState = GlobalState(boardStateRef, gameServiceStateRef)
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
