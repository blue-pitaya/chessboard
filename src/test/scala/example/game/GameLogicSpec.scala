package example.game

import example.game._
import example.models._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import chessboardcore.Vec2d

class GameLogicSpec extends AnyFlatSpec with Matchers {
  "pawn en passant move" should "kill pawn that made 2 tile move" in {
    val blackPawn = Piece(Pawn, Black)
    val whitePawn = Piece(Pawn, White)
    val from = Vec2d(2, 4)
    val to = Vec2d(3, 5)
    val size = Vec2d(8, 8)
    val state = GameState(
      size = Vec2d(8, 8),
      pieces = Map(from -> whitePawn, Vec2d(3, 4) -> blackPawn)
    ).addMoveToHistory(Move(blackPawn, Vec2d(3, 6), Vec2d(3, 4)))
    val expected = GameState(size = size, pieces = Map(to -> whitePawn))
      .addMoveToHistory(Move(blackPawn, Vec2d(3, 6), Vec2d(3, 4)))
      .addMoveToHistory(Move(whitePawn, from, to))

    GameLogic.makeMove(from, to, state).get shouldEqual expected
  }

  "move without piece" should "be impossible" in {
    val state = GameState(size = Vec2d(8, 8), pieces = Map())

    GameLogic.makeMove(Vec2d(0, 0), Vec2d(1, 1), state) shouldEqual None
  }

  "illegal move for certain piece" should "be impossible" in {
    val from = Vec2d(0, 0)
    val state =
      GameState(size = Vec2d(8, 8), pieces = Map(from -> Piece(Knight, White)))

    GameLogic.makeMove(from, Vec2d(2, 2), state) shouldEqual None
  }
}
