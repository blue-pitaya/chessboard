package example.models

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Vec2d(x: Int, y: Int)

object Vec2d {
  def zero: Vec2d = Vec2d(0, 0)

  def matrix(from: Vec2d, to: Vec2d): List[Vec2d] = (from.x to to.x)
    .flatMap(x => (from.y to to.y).map(y => Vec2d(x, y)))
    .toList

  def matrixUntil(from: Vec2d, until: Vec2d): List[Vec2d] =
    matrix(from, until - Vec2d(1, 1))

  implicit class Vec2dExtensions(v: Vec2d) {
    def +(o: Vec2d): Vec2d = Vec2d(v.x + o.x, v.y + o.y)

    def *(card: Int): Vec2d = Vec2d(v.x * card, v.y * card)

    def *(card: Double): Vec2d = Vec2d((v.x * card).toInt, (v.y * card).toInt)

    def -(o: Vec2d): Vec2d = v + (o * -1)
  }
}
