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
    val pc = pieceComponent(pieceDraggingBinds(state.dm, handler), pieceSize)
    val piecesContainerByColor = (c: FigColor) => piecesContainer(c, pieces, pc)

    div(
      width("200px"),
      cls("flex flex-row bg-stone-800"),
      piecesContainerByColor(White),
      piecesContainerByColor(Black)
    )
  }

  def piecesContainer(
      color: FigColor,
      pieces: List[Fig],
      f: (FigColor, Fig) => Element
  ): Element = div(cls("flex flex-col"), pieces.map(p => f(color, p)))

  def pieceDraggingBinds(
      dm: DM[PieceDraggingId],
      handler: Ev => IO[Unit]
  )(color: FigColor, piece: Fig): Seq[Binder.Base] = {
    val pieceDraggingId = PieceDraggingId(piece, color)
    draggingBindings(dm, pieceDraggingId, toEv(pieceDraggingId, handler))
  }

  def draggingBindings[A](
      dm: DM[A],
      id: A,
      handle: Dragging.Event => IO[Unit]
  ): Seq[Binder.Base] = dm.componentBindings(id) ++
    List(dm.componentEvents(id) --> catsRunObserver(handle))

  def catsRunObserver[A](f: A => IO[Unit]): Observer[A] = Observer[A] { e =>
    f(e).unsafeRunAsync { cb =>
      cb match {
        case Left(err)    => dom.console.error(err.toString())
        case Right(value) => ()
      }
    }
  }

  def toEv(
      id: PieceDraggingId,
      handler: Ev => IO[Unit]
  ): Dragging.Event => IO[Unit] = { e =>
    handler(PickerPieceDragging(e, id.piece, id.color))
  }

  def pieceComponent(
      db: (FigColor, Fig) => Seq[Binder.Base],
      pieceSize: Vec2d
  ): (FigColor, Fig) => Element = (c, p) => {
    val imgPath = pieceImgPath(c, p)
    imgElement(imgPath, pieceSize, db(c, p))
  }

  def pieceImgPath(color: FigColor, piece: Fig): String =
    s"/pieces/${toImgPathPart(color)}-${toImgPathPart(piece)}.png"

  def toImgPathPart(v: Fig): String = v.toString().toLowerCase()

  def toImgPathPart(v: FigColor): String = v.toString().toLowerCase()

  def imgElement(
      href: String,
      size: Vec2d,
      bindings: Seq[Binder.Base]
  ): Element = svg.svg(
    svgSizeAttrs(size),
    svg.image(svg.href(href), svgSizeAttrs(size)),
    bindings
  )

  def svgSizeAttrs(v: Vec2d) = List(svg.width(toPx(v.x)), svg.height(toPx(v.y)))

  def toPx(v: Int): String = s"${v}px"

}
