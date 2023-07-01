package example.pages.creator

import com.raquo.laminar.api.L._
import example.pages.creator.Models.PieceColor._
import example.pages.creator.Models.PieceKind._
import org.scalajs.dom

import Models._
import example.game.Vec2d
import dev.bluepitaya.laminardragging.Dragging

object PiecePicker {
  case class PieceToPick(piece: Piece, imgPath: String)

  sealed trait Event
  case class PiecePicked(e: Dragging.Event) extends Event

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
  ): Element = svg.svg(
    svg.width("100"),
    svg.height("100"),
    svg.image(
      svg.href(pieceToPick.imgPath),
      svg.width("100"),
      svg.height("100")
    ),
    draggingModule.componentBindings(pieceToPick.piece),
    draggingModule
      .componentEvents(pieceToPick.piece)
      .map(e => PiecePicked(e)) --> observer
    // onPointerDown
    //  .map(toPosition)
    //  .map(p => PieceDraggingStart(piece = pieceToPick.piece, position = p)) -->
    //  observer
  )

  private def toPosition(e: dom.PointerEvent): Vec2d =
    Vec2d(e.pageX.toInt, e.pageY.toInt)

  // private def getRelativePosition(
  //    e: dom.MouseEvent,
  //    container: dom.Element
  // ): Vec2d = {
  //  val rect = container.getBoundingClientRect()
  //  val x = e.pageX - (rect.x + dom.window.pageXOffset)
  //  val y = e.pageY - (rect.y + dom.window.pageYOffset)

  //  Vec2d(x.toInt, y.toInt)
  // }

}
