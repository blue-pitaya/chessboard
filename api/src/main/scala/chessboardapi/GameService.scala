package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Utils
import chessboardcore.Vec2d
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2

object GameServiceModel {

  case class State2(games: Map[String, TrueGameService.Module])

  case class State(entries: List[Entry])
  object State {
    def init = State(entries = List())

    def initDebug = State(entries =
      List(
        Entry(
          id = "abc",
          game = GameInfo(
            board = Board(
              Vec2d(6, 6),
              List(
                PlacedPiece(pos = Vec2d(0, 0), piece = Piece(White, King)),
                PlacedPiece(pos = Vec2d(5, 5), piece = Piece(Black, King))
              )
            ),
            timeSettings = TimeSettings(180),
            players = Players.init
          ),
          playersInRoom = List()
        )
      )
    )
  }

  case class PlayerInRoom(token: String, name: String)

  case class Entry(
      id: String,
      game: GameInfo,
      playersInRoom: List[PlayerInRoom]
  )

  sealed trait Result
  object Result {
    case object Success extends Result
    case class Error(msg: String) extends Result
  }
}

object GameService {
  import GameServiceModel._

  def createExample(stateRef: Ref[IO, State2]): IO[Unit] = {
    for {
      module <- TrueGameService.create()
      _ <- stateRef.set(State2(Map("abc" -> module)))
    } yield ()
  }

  def join(
      gameId: String,
      stateRef: Ref[IO, State2],
      ws: WebSocketBuilder2[IO]
  ): IO[Response[IO]] = {
    for {
      _ <- IO.println("joined")
      state <- stateRef.get
      gameModule <- IO
        .fromOption(state.games.get(gameId))(new Exception("game not found"))
      resp <- gameModule.subsrice(ws)
    } yield (resp)
  }

  def create(stateRef: Ref[IO, State], board: Board): IO[String] = for {
    id <- Utils.createId()
    entry = createEntry(id, board)
    _ <- stateRef.update(s => s.copy(entries = s.entries.appended(entry)))
  } yield (id)

  def get(stateRef: Ref[IO, State], id: String): IO[Option[Entry]] = for {
    state <- stateRef.get
    entryOpt = state.entries.find(_.id == id)
  } yield (entryOpt)

  // def join(
  //    gameId: String,
  //    token: String,
  //    stateRef: Ref[IO, State]
  // ): IO[Option[GameInfo]] = {
  //  val _addIfNotExists =
  //    Kleisli((s: State) => addIfNotExists(s, gameId, token))
  //  val _gameInfoOf = Kleisli((s: State) => gameInfoOf(s, gameId))

  //  for {
  //    state <- stateRef.get
  //    gameInfoOpt = _addIfNotExists.andThen(_gameInfoOf).run(state)
  //  } yield (gameInfoOpt)
  // }

  // def generateNickname(exisitng: List[String]): IO[String] = ???

  // def addIfNotExists(
  //    state: State,
  //    gameId: String,
  //    userId: String
  // ): Option[State] = ???

  // def gameInfoOf(state: State, gameId: String): Option[GameInfo] = ???

  def createEntry(id: String, board: Board): Entry = {
    val timeSettings = TimeSettings(timePerPlayerInSec = 3 * 60)
    val players = Players.init
    val game = GameInfo(board, timeSettings, players)

    Entry(id, game, List())
  }

}
