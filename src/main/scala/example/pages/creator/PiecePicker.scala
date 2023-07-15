package example.pages.creator

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import example.Misc
import org.scalajs.dom
import example.AppModel

//TODO: bg-stone-800 should be dried

object PiecePickerModel {
  case class Data(dm: AppModel.DM)
}

object PiecePicker {
  import ExAppModel._
  import example.AppModel._
  import PiecePickerModel._

  def component(data: Data, handler: Ev => IO[Unit]) = {
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

  def pieceComponent(
      dm: DM,
      pieceSize: Vec2d,
      piece: Piece,
      handler: Ev => IO[Unit]
  ): Element = {
    val imgPath = Misc.pieceImgPath(piece)
    val draggingId = DraggingId.PickerPiece(piece)

    svg.svg(
      svgSizeAttrs(pieceSize),
      svg.image(svg.href(imgPath), svgSizeAttrs(pieceSize)),
      dm.componentBindings(draggingId),
      dm.componentEvents(draggingId).map(e => PickerPieceDragging(e, piece)) -->
        catsRunObserver(handler)
    )
  }

  def piecesContainer(
      color: PieceColor,
      pieceKinds: List[PieceKind],
      pieceComponent: Piece => Element
  ): Element = div(
    cls("flex flex-col"),
    pieceKinds.map(k => pieceComponent(Piece(color, k)))
  )

  def catsRunObserver[A](f: A => IO[Unit]): Observer[A] = Observer[A] { e =>
    f(e).unsafeRunAsync { cb =>
      cb match {
        case Left(err)    => dom.console.error(err.toString())
        case Right(value) => ()
      }
    }
  }

  def svgSizeAttrs(v: Vec2d) = List(svg.width(toPx(v.x)), svg.height(toPx(v.y)))

  def toPx(v: Int): String = s"${v}px"
}
