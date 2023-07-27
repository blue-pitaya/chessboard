package chessboardapi.game

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Utils
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import chessboardapi.game.GameService

// ( ͠° ͟ʖ ͡°)
// TODO: change name, this is repository not service XD
object GameRepository {
  import GameModel._

  def createState(): IO[Ref[IO, RepositoryState]] = Ref
    .of[IO, RepositoryState](GameModel.RepositoryState(Map()))

  def join(
      gameId: String,
      stateRef: Ref[IO, RepositoryState],
      ws: WebSocketBuilder2[IO]
  ): IO[Response[IO]] = {
    for {
      state <- stateRef.get
      gameModule <- IO.fromOption(state.games.get(gameId))(GameNotFound(gameId))
      resp <- gameModule.join(ws)
    } yield (resp)
  }

  def create(stateRef: Ref[IO, RepositoryState], board: Board): IO[String] =
    for {
      id <- Utils.createId[IO]()
      module <- GameService.create(board)
      _ <- stateRef.update(s => s.copy(games = s.games.updated(id, module)))
    } yield (id)
}
