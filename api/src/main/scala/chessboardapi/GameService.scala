package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Utils
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2

// ( ͠° ͟ʖ ͡°)
object GameServiceModel {
  case class State(games: Map[String, TrueGameService.Module])

  sealed trait Fail extends Throwable
  case class GameNotFound(id: String) extends Fail
}

object GameService {
  import GameServiceModel._

  def createState(): IO[Ref[IO, State]] = Ref
    .of[IO, State](GameServiceModel.State(Map()))

  def createExample(stateRef: Ref[IO, State]): IO[Unit] = {
    for {
      module <- TrueGameService.create(Examples.board)
      _ <- stateRef.set(State(Map("abc" -> module)))
    } yield ()
  }

  def join(
      gameId: String,
      stateRef: Ref[IO, State],
      ws: WebSocketBuilder2[IO]
  ): IO[Response[IO]] = {
    for {
      state <- stateRef.get
      gameModule <- IO.fromOption(state.games.get(gameId))(GameNotFound(gameId))
      resp <- gameModule.subsrice(ws)
    } yield (resp)
  }

  def create(stateRef: Ref[IO, State], board: Board): IO[String] = for {
    id <- Utils.createId()
    module <- TrueGameService.create(board)
    _ <- stateRef.update(s => s.copy(games = s.games.updated(id, module)))
  } yield (id)
}
