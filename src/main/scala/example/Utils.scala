package example

import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.unsafe.implicits.global
import chessboardcore.Model
import chessboardcore.Vec2d
import org.scalajs.dom

object Utils {
  def toPx(n: Int) = s"${n}px"

  def run(f: IO[Unit]): Unit = f.unsafeRunAsync { cb =>
    cb match {
      case Left(err)    => dom.console.error(err)
      case Right(value) => ()
    }
  }

  def catsUnsafeRunSync[A](f: SyncIO[A]): A = f.unsafeRunSync()

  def getRelativePosition(
      e: dom.PointerEvent,
      container: dom.Element
  ): Vec2d = {
    val rect = container.getBoundingClientRect()
    val x = e.pageX - (rect.x + dom.window.pageXOffset)
    val y = e.pageY - (rect.y + dom.window.pageYOffset)

    Vec2d(x.toInt, y.toInt)
  }

  def pieceImgPath(piece: Model.Piece): String = {
    val colorPart = piece.color.toString().toLowerCase()
    val piecePart = piece.kind.toString().toLowerCase()

    s"/pieces/${colorPart}-${piecePart}.png"
  }

  def isPointerInsideElement(
      e: dom.PointerEvent,
      container: dom.Element
  ): Boolean = {
    val rect = container.getBoundingClientRect()
    val x = e.pageX - (rect.x + dom.window.pageXOffset)
    val y = e.pageY - (rect.y + dom.window.pageYOffset)

    (x >= 0) && (y >= 0) && (x < rect.width) && (y < rect.height)
  }
}
