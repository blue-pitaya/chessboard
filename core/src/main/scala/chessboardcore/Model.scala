package chessboardcore

object Model {
  sealed trait Fig
  case object Pawn extends Fig
  case object Rook extends Fig
  case object Knight extends Fig
  case object Bishop extends Fig
  case object Queen extends Fig
  case object King extends Fig

  sealed trait FigColor
  case object White extends FigColor
  case object Black extends FigColor

  case class Piece(color: FigColor, kind: Fig)

  case class PlacedPiece(pos: Vec2d, piece: Piece)
}
