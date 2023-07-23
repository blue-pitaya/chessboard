package example

import chessboardcore.Vec2d
import cats.effect.IO
import org.scalajs.dom
import cats.effect.unsafe.implicits.global
import cats.effect.SyncIO
import scala.util.Try
import scala.util.Failure
import scala.util.Success

object Utils {
  def transformStr(v: Vec2d) = s"translate(${v.x}, ${v.y})"

  def toPx(n: Int) = s"${n}px"

  def getTileSize(logicSize: Vec2d, renderSizeInPx: Vec2d): Vec2d =
    Vec2d(renderSizeInPx.x / logicSize.x, renderSizeInPx.y / logicSize.y)

  def toRealPos(
      logicPos: Vec2d,
      logicSize: Vec2d,
      renderSizeInPx: Vec2d
  ): Vec2d = {
    val tileSize = getTileSize(logicSize, renderSizeInPx)
    val xInPx = logicPos.x * tileSize.x
    // note: board positions and render positions has opposite y axis
    val yInPx = renderSizeInPx.y - tileSize.y - (logicPos.y * tileSize.y)

    Vec2d(xInPx, yInPx)
  }

  def toLogicPostion(
      pos: Vec2d,
      logicSize: Vec2d,
      renderSizeInPx: Vec2d
  ): Vec2d = Vec2d(
    pos.x / getTileSize(logicSize, renderSizeInPx).x,
    (renderSizeInPx.y - pos.y) / getTileSize(logicSize, renderSizeInPx).y
  )

  def run(f: IO[Unit]): Unit = f.unsafeRunAsync { cb =>
    cb match {
      case Left(err)    => dom.console.error(err)
      case Right(value) => ()
    }
  }

  def catsRun[A](f: A => IO[Unit]): A => Unit = { e =>
    run(f(e))
  }

  def catsRunSync[A](f: SyncIO[A], callBk: A => Unit): Unit =
    Try(f.unsafeRunSync()) match {
      case Failure(exception) => dom.console.error(exception)
      case Success(value)     => callBk(value)
    }

  def catsUnsafeRunSync[A](f: SyncIO[A]): A = f.unsafeRunSync()
}
