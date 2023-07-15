package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._

import scala.util.Random
import chessboardcore.Vec2d

object GameServiceModel {

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
          )
        )
      )
    )
  }

  case class Entry(id: String, game: GameInfo)

  sealed trait Result
  object Result {
    case object Success extends Result
    case class Error(msg: String) extends Result
  }
}

object GameService {
  import GameServiceModel._

  def create(stateRef: Ref[IO, State], board: Board): IO[String] = for {
    id <- createId()
    entry = createEntry(id, board)
    _ <- stateRef.update(s => s.copy(entries = s.entries.appended(entry)))
  } yield (id)

  def get(stateRef: Ref[IO, State], id: String): IO[Option[Entry]] = for {
    state <- stateRef.get
    entryOpt = state.entries.find(_.id == id)
  } yield (entryOpt)

  def createEntry(id: String, board: Board): Entry = {
    val timeSettings = TimeSettings(timePerPlayerInSec = 3 * 60)
    val players = Players.init
    val game = GameInfo(board, timeSettings, players)

    Entry(id, game)
  }

  def createId(): IO[String] = IO(
    (0 until 32).foldLeft("") { case (acc, _) =>
      acc ++ Random.nextInt(16).toHexString
    }
  )

}
