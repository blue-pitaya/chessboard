package example.game

import example.GameState
import example.models.Bishop
import example.models.Black
import example.models.HexColor
import example.models.King
import example.models.Knight
import example.models.Pawn
import example.models.Piece
import example.models.Queen
import example.models.Rook
import example.models.Vec2d
import example.models.White
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.scalajs.js.JSConverters._

class RendererSpec extends AnyFlatSpec with Matchers {
  val boardSize: Vec2d = Vec2d(8, 8)

  "knightMoves" should "be in shape of L" in {
    val piece = Piece(Knight, White)
    val state = GameState(
      size = boardSize,
      pieces = Map(
        Vec2d(4, 3) -> piece,
        Vec2d(2, 2) -> Piece(Pawn, White),
        Vec2d(2, 4) -> Piece(Pawn, Black)
      )
    )
    val expected = Set(
      Vec2d(2, 4),
      Vec2d(3, 5),
      Vec2d(3, 1),
      Vec2d(5, 5),
      Vec2d(5, 1),
      Vec2d(6, 2),
      Vec2d(6, 4)
    )

    PossibleMoves.getMoves(Vec2d(4, 3), piece, state) shouldEqual expected
  }

  "bishopMoves" should "be diagonal and blocked by pieces" in {
    val piece = Piece(Bishop, White)
    val state = GameState(
      size = Vec2d(5, 4),
      pieces = Map(
        Vec2d(2, 1) -> piece,
        Vec2d(0, 3) -> Piece(Pawn, White),
        Vec2d(1, 0) -> Piece(Pawn, Black)
      )
    )
    val expected =
      Set(Vec2d(1, 2), Vec2d(1, 0), Vec2d(3, 0), Vec2d(3, 2), Vec2d(4, 3))

    PossibleMoves.getMoves(Vec2d(2, 1), piece, state) shouldEqual expected
  }

  "rookMoves" should "be straight and blocked by pieces" in {
    val piece = Piece(Rook, White)
    val state = GameState(
      size = Vec2d(4, 4),
      pieces = Map(
        Vec2d(2, 1) -> piece,
        Vec2d(0, 1) -> Piece(Pawn, White),
        Vec2d(2, 3) -> Piece(Pawn, Black)
      )
    )
    val expected =
      Set(Vec2d(1, 1), Vec2d(2, 0), Vec2d(3, 1), Vec2d(2, 2), Vec2d(2, 3))

    PossibleMoves.getMoves(Vec2d(2, 1), piece, state) shouldEqual expected
  }

  "queenMoves" should "be straight, diagonal and blocked by pieces" in {
    val piece = Piece(Queen, White)
    val state = GameState(
      size = Vec2d(4, 4),
      pieces = Map(
        Vec2d(2, 1) -> piece,
        Vec2d(0, 1) -> Piece(Pawn, White),
        Vec2d(2, 3) -> Piece(Pawn, Black)
      )
    )
    val expected = Set(
      Vec2d(1, 0),
      Vec2d(2, 0),
      Vec2d(3, 0),
      Vec2d(1, 1),
      Vec2d(3, 1),
      Vec2d(1, 2),
      Vec2d(2, 2),
      Vec2d(3, 2),
      Vec2d(0, 3),
      Vec2d(2, 3)
    )

    PossibleMoves.getMoves(Vec2d(2, 1), piece, state) shouldEqual expected
  }

  "king" should "not be able to walk to other king" in {
    val piece = Piece(King, White)
    val state = GameState(
      size = Vec2d(3, 4),
      pieces = Map(
        Vec2d(0, 1) -> Piece(King, Black),
        Vec2d(2, 1) -> piece,
        Vec2d(1, 3) -> Piece(Rook, White),
        Vec2d(2, 0) -> Piece(Pawn, White)
      )
    )
    val expected = Set(Vec2d(2, 2))

    PossibleMoves.getMoves(Vec2d(2, 1), piece, state) shouldEqual expected
  }

  "king" should "not be able to walk to position which is under attack" in {
    val piece = Piece(King, White)
    val state = GameState(
      size = Vec2d(3, 3),
      pieces = Map(
        Vec2d(0, 0) -> piece,
        Vec2d(0, 2) -> Piece(Knight, Black),
        Vec2d(2, 0) -> Piece(Bishop, Black)
      )
    )
    val expected = Set(Vec2d(0, 1))

    PossibleMoves.getMoves(Vec2d(0, 0), piece, state) shouldEqual expected
  }

  "king" should
    "not be able to take piece which is in position that is under attack by another piece" in {
      val piece = Piece(King, White)
      val state = GameState(
        size = Vec2d(2, 2),
        pieces = Map(
          Vec2d(0, 0) -> piece,
          Vec2d(0, 1) -> Piece(Pawn, Black),
          Vec2d(1, 1) -> Piece(Rook, Black)
        )
      )
      val expected = Set(Vec2d(1, 1))

      PossibleMoves.getMoves(Vec2d(0, 0), piece, state) shouldEqual expected
    }

  "king" should
    "not be able to walk to position which is under attack by pawn" in {
      val piece = Piece(King, White)
      val state = GameState(
        size = Vec2d(3, 3),
        pieces = Map(
          Vec2d(1, 1) -> piece,
          Vec2d(0, 1) -> Piece(Pawn, Black),
          Vec2d(1, 2) -> Piece(Pawn, Black),
          Vec2d(2, 1) -> Piece(Pawn, Black)
        )
      )
      val expected =
        Set(Vec2d(0, 0), Vec2d(2, 0), Vec2d(0, 2), Vec2d(1, 2), Vec2d(2, 2))

      PossibleMoves.getMoves(Vec2d(1, 1), piece, state) shouldEqual expected
    }
}
