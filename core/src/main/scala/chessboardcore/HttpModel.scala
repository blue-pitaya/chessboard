package chessboardcore

import Model._

object HttpModel {
  case class CreateChessboard_In(boardSize: Vec2d, pieces: List[PlacedPiece])
}
