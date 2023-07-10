package chessboardapi

import cats.effect.kernel.Ref
import cats.effect.IO

object ChessboardRepository {
  case class State()

  def create(): Ref[IO, State] = ???
}
