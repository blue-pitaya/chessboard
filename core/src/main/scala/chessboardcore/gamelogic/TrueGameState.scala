package example.game

import chessboardcore.Vec2d
import chessboardcore.Model._

sealed trait GameMove
final case class TrueMove(piece: Piece, from: Vec2d, to: Vec2d) extends GameMove
final case class CastlingMove(kingMove: TrueMove, rookMove: TrueMove)
    extends GameMove

case class TrueGameState(
    size: Vec2d,
    pieces: Map[Vec2d, Piece],
    moveHistory: Vector[GameMove] = Vector()
) {
  val lastMove: Option[GameMove] = moveHistory.lastOption

  def updatePieces(v: Map[Vec2d, Piece]) = copy(pieces = v)

  def addMoveToHistory(move: GameMove) =
    copy(moveHistory = moveHistory.appended(move))

  def hasPieceMoved(startPos: Vec2d) = moveHistory.exists {
    case TrueMove(piece, from, to) => startPos == from
    case CastlingMove(kingMove, rookMove) => startPos == kingMove.from ||
      startPos == rookMove.from
  }

  def movePiece(move: TrueMove) =
    copy(pieces = pieces.removed(move.from).updated(move.to, move.piece))
}

object TrueGameState {
  def standardBoard = TrueGameState(
    size = Vec2d(8, 8),
    pieces = standardPieces,
    moveHistory = Vector()
  )

  private def standardPieces = {
    val whitePawns = (0 until 8).map { x =>
      val pos = Vec2d(x, 1)
      (pos -> Piece(White, Pawn))
    }
    val whiteMajorPieces = Seq(
      (Vec2d(0, 0) -> Piece(White, Rook)),
      (Vec2d(1, 0) -> Piece(White, Knight)),
      (Vec2d(2, 0) -> Piece(White, Bishop)),
      (Vec2d(3, 0) -> Piece(White, Queen)),
      (Vec2d(4, 0) -> Piece(White, King)),
      (Vec2d(5, 0) -> Piece(White, Bishop)),
      (Vec2d(6, 0) -> Piece(White, Knight)),
      (Vec2d(7, 0) -> Piece(White, Rook))
    )
    val whitePieces = whitePawns ++ whiteMajorPieces
    val blackPieces = whitePieces.map { case (pos, piece) =>
      Vec2d(pos.x, 7 - pos.y) -> Piece(Black, piece.kind)
    }

    (whitePieces ++ blackPieces).toMap
  }
}
