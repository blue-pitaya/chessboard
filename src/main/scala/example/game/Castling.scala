package example.game

import example.Move
import example.models.Black
import example.models.Piece
import example.models.PieceColor
import example.models.White
import xyz.bluepitaya.common.Vec2d

// assuming standard chessboard setup
object Castling {
  private def numsBetweenInc(a: Int, b: Int) = {
    val start = Math.min(a, b)
    val end = Math.max(a, b)
    (start to end)
  }

  private def numsBetweenExc(a: Int, b: Int) = {
    val start = Math.min(a, b) + 1
    val end = Math.max(a, b)
    (start until end)
  }

  private def kingMove(kingPos: Vec2d, rookPos: Vec2d): Vec2d = {
    val step =
      if (kingPos.x > rookPos.x) Vec2d(-2, 0)
      else Vec2d(2, 0)

    kingPos + step
  }

  private def kingPath(kingPos: Vec2d, rookPos: Vec2d) = {
    val a = kingPos.x
    val b = kingMove(kingPos, rookPos).x
    val xs = numsBetweenInc(a, b)

    xs.map(x => Vec2d(x, kingPos.y))
  }

  // assuming king and rook exists and are same color
  def getCastleKingMove(
      kingPos: Vec2d,
      rookPos: Vec2d,
      hasMoved: Vec2d => Boolean,
      isPieceOn: Vec2d => Boolean,
      isAttackOn: Vec2d => Boolean
  ): Option[Vec2d] = {
    lazy val arePiecesOnSameRank = kingPos.y == rookPos.y
    lazy val hasKingMoved = hasMoved(kingPos)
    lazy val hasRookMoved = hasMoved(rookPos)

    lazy val pathBetween = numsBetweenExc(kingPos.x, rookPos.x)
      .map(x => Vec2d(x, kingPos.y))
    lazy val arePiecesBetween = pathBetween.exists(isPieceOn)

    lazy val isKingCheckedOnPath = kingPath(kingPos, rookPos).exists(isAttackOn)

    Option.when(
      arePiecesOnSameRank && !hasKingMoved && !hasRookMoved &&
        !arePiecesBetween && !isKingCheckedOnPath
    )(kingMove(kingPos, rookPos))
  }

  def getMoves(
      color: PieceColor,
      kingPos: Vec2d,
      rooks: Map[Vec2d, Piece],
      castleMove: (Vec2d, Vec2d) => Option[Vec2d]
  ): Set[Vec2d] = {
    rooks
      .toSet[(Vec2d, Piece)]
      .flatMap { case (rookPos, rook) =>
        if (rook.color != color) Set()
        else Set(castleMove(kingPos, rookPos)).flatten
      }
  }
}
