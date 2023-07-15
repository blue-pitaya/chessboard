package example.pages.creator

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.Misc
import org.scalajs.dom

//TODO: bg-stone-800 should be dried

object ExApp {
  import ExAppModel._
  type DM[A] = Dragging.DraggingModule[A]

  def component(state: State, handler: Ev => IO[Unit]) = {
    val pieces: List[PieceKind] = List(Pawn, Rook, Knight, Bishop, Queen, King)
    val pieceSize = Vec2d(100, 100)
    val _pieceComponent = (c: PieceColor, p: PieceKind) =>
      pieceComponent(state.dm, pieceSize, c, p, handler)
    val piecesContainerByColor =
      (c: PieceColor) => piecesContainer(c, pieces, _pieceComponent)

    div(
      cls("flex flex-row bg-stone-800"),
      piecesContainerByColor(White),
      piecesContainerByColor(Black)
    )
  }

  def pieceComponent(
      dm: DM[PieceDraggingId],
      pieceSize: Vec2d,
      color: PieceColor,
      pieceKind: PieceKind,
      handler: Ev => IO[Unit]
  ): Element = {
    val piece = Piece(color, pieceKind)
    val imgPath = Misc.pieceImgPath(piece)
    val draggingId = PickerPieceDraggingId(pieceKind, color)

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
      pieces: List[PieceKind],
      pieceComponent: (PieceColor, PieceKind) => Element
  ): Element =
    div(cls("flex flex-col"), pieces.map(p => pieceComponent(color, p)))

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
