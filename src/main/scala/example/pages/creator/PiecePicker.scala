package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d
import example.pages.creator.Models.PieceKind._
import org.scalajs.dom

import Models._
import example.pages.creator.logic.BoardUiLogic
import example.pages.creator.logic.DraggingId

object PiecePicker {
  case class PieceToPick(piece: Piece, imgPath: String)

  def component(
      observer: Observer[BoardUiLogic.Event],
      draggingModule: Dragging.DraggingModule[DraggingId]
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

  def pieceToPick(piece: Piece): PieceToPick =
    PieceToPick(piece = piece, imgPath = BoardUiLogic.pieceImgPath(piece))

  def renderPieceToPick(
      pieceToPick: PieceToPick,
      draggingModule: Dragging.DraggingModule[DraggingId],
      observer: Observer[BoardUiLogic.Event]
  ): Element = pieceImgElement(pieceToPick.piece).amend(
    draggingModule
      .componentBindings(DraggingId.PieceOnPicker(pieceToPick.piece)),
    draggingModule
      .componentEvents(DraggingId.PieceOnPicker(pieceToPick.piece))
      .map(e =>
        BoardUiLogic.PieceDragging(
          DraggingId.PieceOnPicker(pieceToPick.piece),
          pieceToPick.piece,
          e
        )
      ) --> observer
  )

  val pieceImgWidthInPx = 100
  val pieceImgHeightInPx = 100

  def pieceImgElement(p: Models.Piece): Element = {
    val imgPath = BoardUiLogic.pieceImgPath(p)

    svg.svg(
      // tailwind values cant be interpolated
      svg.cls(s"w-[100px] h-[100px]"),
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
