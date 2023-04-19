package example

import xyz.bluepitaya.common.Vec2d
import xyz.bluepitaya.common.Vec2f
import com.raquo.laminar.api.L._
import org.scalajs.dom

object Utils {
  def takeWhileInclusive[A](
      start: A,
      next: A => A,
      stopInc: A => Boolean,
      stopExc: A => Boolean
  ) = {
    def _f(curr: A, acc: Seq[A]): Seq[A] =
      if (stopExc(curr)) acc
      else if (stopInc(curr)) curr +: acc
      else _f(next(curr), curr +: acc)

    _f(start, Seq())
  }

  def transformStr(v: Vec2d) = s"translate(${v.x}, ${v.y})"
  def transformCenterStr(v: Vec2d) = s"translate(${v.x}, ${v.y})"
  def toPx(n: Int) = s"${n}px"
}
