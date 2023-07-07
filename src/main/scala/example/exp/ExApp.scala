package example.exp

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.game.Vec2d
import org.scalajs.dom

object ExApp {
  import ExAppModel._
  type DM[A] = Dragging.DraggingModule[A]

  def component(state: State, handler: Ev => IO[Unit]) = {
    val pieces: List[Fig] = List(Pawn, Rook, Knight, Bishop, Queen, King)
    val pieceSize = Vec2d(100, 100)
    val _pieceComponent = (c: FigColor, p: Fig) =>
      pieceComponent(state.dm, pieceSize, c, p, handler)
    val piecesContainerByColor =
      (c: FigColor) => piecesContainer(c, pieces, _pieceComponent)

    div(
      width("200px"),
      cls("flex flex-row bg-stone-800"),
      piecesContainerByColor(White),
      piecesContainerByColor(Black)
    )
  }

  def pieceComponent(
      dm: DM[PieceDraggingId],
      pieceSize: Vec2d,
      color: FigColor,
      piece: Fig,
      handler: Ev => IO[Unit]
  ): Element = {
    val imgPath = pieceImgPath(color, piece)
    val draggingId = PieceDraggingId(piece, color)

    svg.svg(
      svgSizeAttrs(pieceSize),
      svg.image(svg.href(imgPath), svgSizeAttrs(pieceSize)),
      dm.componentBindings(draggingId),
      dm.componentEvents(draggingId)
        .map(e => PickerPieceDragging(e, piece, color)) -->
        catsRunObserver(handler)
    )
  }

  def piecesContainer(
      color: FigColor,
      pieces: List[Fig],
      pieceComponent: (FigColor, Fig) => Element
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

  def pieceImgPath(color: FigColor, piece: Fig): String = {
    val colorPart = color.toString().toLowerCase()
    val piecePart = piece.toString().toLowerCase()

    s"/pieces/${colorPart}-${piecePart}.png"
  }

  def svgSizeAttrs(v: Vec2d) = List(svg.width(toPx(v.x)), svg.height(toPx(v.y)))

  def toPx(v: Int): String = s"${v}px"
}
