package example.pages.creator

import com.raquo.laminar.api.L._
import example.pages.creator.Models.PieceColor._
import example.pages.creator.Models.PieceKind._

import Models._

object PiecePicker {
  case class PieceToPick(piece: Piece, imgPath: String)

  sealed trait Event
  case class PieceDraggingStart(piece: Piece) extends Event

  def component(observer: Observer[Event]): Element = {
    val whitePieces = pieces(PieceColor.White).map(pieceToPick)
    val blackPieces = pieces(PieceColor.Black).map(pieceToPick)

    div(
      width("200px"),
      cls("flex flex-row bg-stone-800"),
      div(
        cls("flex flex-col"),
        whitePieces.map(v => renderPieceToPick(v, observer))
      ),
      div(
        cls("flex flex-col"),
        blackPieces.map(v => renderPieceToPick(v, observer))
      )
    )
  }

  def pieces(color: PieceColor): List[Piece] =
    List(Pawn, Rook, Knight, Bishop, Queen, King)
      .map(v => Piece(kind = v, color = color))

  def pieceImgPath(piece: Piece): String = {
    val piecePart = piece.kind match {
      case Queen  => "queen"
      case Rook   => "rook"
      case Pawn   => "pawn"
      case Bishop => "bishop"
      case King   => "king"
      case Knight => "knight"
    }
    val colorPart = piece.color match {
      case Black => "black"
      case White => "white"
    }

    s"/pieces/${colorPart}-${piecePart}.png"
  }

  def pieceToPick(piece: Piece): PieceToPick =
    PieceToPick(piece = piece, imgPath = pieceImgPath(piece))

  def renderPieceToPick(
      pieceToPick: PieceToPick,
      observer: Observer[Event]
  ): Element = svg.svg(
    svg.width("100"),
    svg.height("100"),
    svg
      .image(svg.href(pieceToPick.imgPath), svg.width("100"), svg.height("100"))
  )
}
