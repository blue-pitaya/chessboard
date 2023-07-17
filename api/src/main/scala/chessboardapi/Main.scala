package chessboardapi

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref

case class GlobalState(
    boardStateRef: Ref[IO, ChessboardRepository.State],
    gameServiceStateRef: Ref[IO, GameServiceModel.State],
    state2Ref: Ref[IO, GameServiceModel.State2]
)

object Main extends IOApp.Simple {

  def initState(): IO[GlobalState] = for {
    boardStateRef <- ChessboardRepository.createState()
    gameServiceStateRef <- Ref
      .of[IO, GameServiceModel.State](GameServiceModel.State.initDebug)
    state2Ref <- Ref
      .of[IO, GameServiceModel.State2](GameServiceModel.State2(Map()))
    globalState = GlobalState(boardStateRef, gameServiceStateRef, state2Ref)
  } yield (globalState)

  override def run: IO[Unit] = debugRun()

  private def debugRun(): IO[Unit] = for {
    globalState <- initState()
    _ <- GameService.createExample(globalState.state2Ref)
    _ <- Server
      .create(globalState)
      .use { _ =>
        IO.readLine >> IO.unit
      }
  } yield ()

}
