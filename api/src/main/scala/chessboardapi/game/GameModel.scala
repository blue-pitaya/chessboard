package chessboardapi.game

object GameModel {
  case class RepositoryState(games: Map[String, GameService.Module])

  sealed trait Fail extends Throwable
  case class GameNotFound(id: String) extends Fail
  case class MakeMoveFail(msg: String) extends Fail
}
