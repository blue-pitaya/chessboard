package example

import chessboardcore.Model

object Misc {
  def pieceImgPath(piece: Model.Piece): String = {
    val colorPart = piece.color.toString().toLowerCase()
    val piecePart = piece.kind.toString().toLowerCase()

    s"/pieces/${colorPart}-${piecePart}.png"
  }
}
