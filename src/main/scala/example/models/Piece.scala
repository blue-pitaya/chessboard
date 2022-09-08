package example.models

sealed trait PieceKind
sealed trait MajorPieceType extends PieceKind
final case object Pawn extends PieceKind
final case object Rook extends MajorPieceType
final case object Bishop extends MajorPieceType
final case object Knight extends MajorPieceType
final case object Queen extends MajorPieceType
final case object King extends PieceKind

sealed trait PieceColor {
  def opposite: PieceColor
}
final case object White extends PieceColor {
  override def opposite: PieceColor = Black
}
final case object Black extends PieceColor {
  override def opposite: PieceColor = White
}

case class Piece(kind: PieceKind, color: PieceColor)
