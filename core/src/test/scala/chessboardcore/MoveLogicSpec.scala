package chessboardcore

import chessboardcore.Model._
import chessboardcore.gamelogic.MoveLogic

class MoveLogicSpec extends munit.FunSuite {
  test("xd") {
    val board = Board(
      size = Vec2d(6, 6),
      pieces = Map(
        Vec2d(5, 0) -> Piece(White, King),
        Vec2d(4, 0) -> Piece(White, Queen),
        Vec2d(0, 5) -> Piece(Black, King),
        Vec2d(1, 5) -> Piece(Black, Queen)
      )
    )

    val res = MoveLogic.canMove(board, Vec2d(4, 0), Vec2d(3, 0))

    assertEquals(res, true)
  }
}
