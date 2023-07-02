package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d
import example.pages.creator.Models.PieceColor._
import example.pages.creator.Models.PieceKind._
import org.scalajs.dom

import Models._

object PiecePicker {
  case class PieceToPick(piece: Piece, imgPath: String)

  sealed trait Event
  case class PiecePicked(p: Piece, e: Dragging.Event) extends Event

  def component(
      observer: Observer[Event],
      draggingModule: Dragging.DraggingModule[Piece]
  ): Element = {
    val whitePieces = pieces(PieceColor.White).map(pieceToPick)
    val blackPieces = pieces(PieceColor.Black).map(pieceToPick)

    div(
      width("200px"),
      cls("flex flex-row bg-stone-800"),
      div(
        cls("flex flex-col"),
        whitePieces.map(v => renderPieceToPick(v, draggingModule, observer))
      ),
      div(
        cls("flex flex-col"),
        blackPieces.map(v => renderPieceToPick(v, draggingModule, observer))
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
      draggingModule: Dragging.DraggingModule[Piece],
      observer: Observer[Event]
  ): Element = pieceImgElement(pieceToPick.piece).amend(
    draggingModule.componentBindings(pieceToPick.piece),
    draggingModule
      .componentEvents(pieceToPick.piece)
      .map(e => PiecePicked(pieceToPick.piece, e)) --> observer
  )

  val pieceImgWidthInPx = 100
  val pieceImgHeightInPx = 100

  def pieceImgElement(p: Models.Piece): Element = {
    val imgPath = pieceImgPath(p)

    svg.svg(
      svg.cls(s"w-[${pieceImgWidthInPx}px] h-[${pieceImgHeightInPx}px]"),
      svg.image(
        svg.href(imgPath),
        svg.width(pieceImgWidthInPx.toString()),
        svg.height(pieceImgHeightInPx.toString())
      )
    )
  }

  private def toPosition(e: dom.PointerEvent): Vec2d =
    Vec2d(e.pageX.toInt, e.pageY.toInt)
}
