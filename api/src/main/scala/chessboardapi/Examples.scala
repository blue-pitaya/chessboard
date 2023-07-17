package chessboardapi

import chessboardcore.Model._
import chessboardcore.Vec2d

object Examples {
  def board = Board(
    Vec2d(6, 6),
    List(
      PlacedPiece(pos = Vec2d(0, 0), piece = Piece(White, King)),
      PlacedPiece(pos = Vec2d(5, 5), piece = Piece(Black, King))
    )
  )
}
