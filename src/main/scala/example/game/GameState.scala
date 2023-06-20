package example.game

import example.models.Piece

sealed trait GameMove
final case class Move(piece: Piece, from: Vec2d, to: Vec2d) extends GameMove
final case class CastlingMove(kingMove: Move, rookMove: Move) extends GameMove

case class GameState(
    size: Vec2d,
    pieces: Map[Vec2d, Piece],
    moveHistory: Vector[GameMove] = Vector()
) {
  val lastMove: Option[GameMove] = moveHistory.lastOption

  def updatePieces(v: Map[Vec2d, Piece]) = copy(pieces = v)

  def addMoveToHistory(move: GameMove) =
    copy(moveHistory = moveHistory.appended(move))

  def hasPieceMoved(startPos: Vec2d) = moveHistory.exists {
    case Move(piece, from, to) => startPos == from
    case CastlingMove(kingMove, rookMove) => startPos == kingMove.from ||
      startPos == rookMove.from
  }

  def movePiece(move: Move) =
    copy(pieces = pieces.removed(move.from).updated(move.to, move.piece))
}

object GameState {
  def standardBoard = GameState(
    size = Vec2d(8, 8),
    pieces = standardPieces,
    moveHistory = Vector()
  )

  private def standardPieces = {
    import example.models._

    val whitePawns = (0 until 8).map { x =>
      val pos = Vec2d(x, 1)
      (pos -> Piece(Pawn, White))
    }
    val whiteMajorPieces = Seq(
      (Vec2d(0, 0) -> Piece(Rook, White)),
      (Vec2d(1, 0) -> Piece(Knight, White)),
      (Vec2d(2, 0) -> Piece(Bishop, White)),
      (Vec2d(3, 0) -> Piece(Queen, White)),
      (Vec2d(4, 0) -> Piece(King, White)),
      (Vec2d(5, 0) -> Piece(Bishop, White)),
      (Vec2d(6, 0) -> Piece(Knight, White)),
      (Vec2d(7, 0) -> Piece(Rook, White))
    )
    val whitePieces = whitePawns ++ whiteMajorPieces
    val blackPieces = whitePieces.map { case (pos, piece) =>
      Vec2d(pos.x, 7 - pos.y) -> Piece(piece.kind, Black)
    }

    (whitePieces ++ blackPieces).toMap
  }
}
