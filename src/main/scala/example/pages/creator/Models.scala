package example.pages.creator

object Models {
  sealed trait PieceKind
  object PieceKind {
    final case object Pawn extends PieceKind
    final case object Rook extends PieceKind
    final case object Bishop extends PieceKind
    final case object Knight extends PieceKind
    final case object Queen extends PieceKind
    final case object King extends PieceKind
  }

  sealed trait PieceColor
  object PieceColor {
    final case object White extends PieceColor
    final case object Black extends PieceColor
  }

  case class Piece(kind: PieceKind, color: PieceColor)
}
