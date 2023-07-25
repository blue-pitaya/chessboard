package example.pages.creator

import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.AppModel
import example.Utils

//TODO: bg-stone-800 should be dried

object PiecePickerComponent {
  case class Data(dm: AppModel.DM)

  import CreatorPageModel._
  import example.AppModel._

  def create(data: Data, handler: Observer[Event]) = {
    val pieceKinds: List[PieceKind] =
      List(Pawn, Rook, Knight, Bishop, Queen, King)
    val pieceSize = Vec2d(100, 100)
    val _pieceComponent =
      (p: Piece) => pieceComponent(data.dm, pieceSize, p, handler)
    val piecesContainerByColor =
      (c: PieceColor) => piecesContainer(c, pieceKinds, _pieceComponent)

    div(
      cls("flex flex-row bg-stone-800"),
      piecesContainerByColor(White),
      piecesContainerByColor(Black)
    )
  }

  private def pieceComponent(
      dm: DM,
      pieceSize: Vec2d,
      piece: Piece,
      handler: Observer[Event]
  ): Element = {
    val imgPath = Utils.pieceImgPath(piece)
    val draggingId = DraggingId.PickerPiece(piece)
    val svgSizeAttrs = List(
      svg.width(Utils.toPx(pieceSize.x)),
      svg.height(Utils.toPx(pieceSize.y))
    )

    svg.svg(
      svgSizeAttrs,
      svg.image(svg.href(imgPath), svgSizeAttrs),
      dm.componentBindings(draggingId),
      dm.componentEvents(draggingId).map(e => PickerPieceDragging(e, piece)) -->
        handler
    )
  }

  private def piecesContainer(
      color: PieceColor,
      pieceKinds: List[PieceKind],
      pieceComponent: Piece => Element
  ): Element = div(
    cls("flex flex-col"),
    pieceKinds.map(k => pieceComponent(Piece(color, k)))
  )

}
