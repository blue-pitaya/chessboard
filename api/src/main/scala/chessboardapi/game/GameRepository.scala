package chessboardapi.game

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardapi.game.GameService
import chessboardcore.Model._
import chessboardcore.Utils
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2

object GameRepositoryModel {
  case class State(games: Map[String, GameServiceModel.Module])
  object State {
    def init = State(Map())
  }

  case class GameNotFound(id: String) extends Throwable
}

object GameRepository {
  import GameRepositoryModel._

  def create(stateRef: Ref[IO, State], board: Board): IO[String] = for {
    id <- Utils.createId[IO]()
    module <- GameService.create(board)
    _ <- stateRef.update(s => s.copy(games = s.games.updated(id, module)))
  } yield (id)

  def join(
      gameId: String,
      stateRef: Ref[IO, State],
      ws: WebSocketBuilder2[IO]
  ): IO[Response[IO]] = {
    for {
      state <- stateRef.get
      gameModule <- IO.fromOption(state.games.get(gameId))(GameNotFound(gameId))
      resp <- gameModule.join(ws)
    } yield (resp)
  }
}
