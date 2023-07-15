package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._

import scala.util.Random

object GameServiceModel {

  case class State(entries: List[Entry])
  object State {
    def init = State(entries = List())
  }

  case class Entry(id: String, game: Game)

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
    val game = Game(board, timeSettings, players)

    Entry(id, game)
  }

  def createId(): IO[String] = IO(
    (0 until 32).foldLeft("") { case (acc, _) =>
      acc ++ Random.nextInt(16).toHexString
    }
  )

}
