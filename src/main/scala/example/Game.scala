package example

import example.models._

object Game {
  def initPieces: Map[Vec2d, Piece] = {
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
