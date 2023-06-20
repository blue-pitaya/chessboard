package example.game

case class Vec2d(x: Int, y: Int) {
  def +(v: Vec2d): Vec2d = Vec2d(x + v.x, y + v.y)
  def -(v: Vec2d): Vec2d = Vec2d(x - v.x, y - v.y)
  def *(n: Int): Vec2d = Vec2d(x * n, y * n)
  def *(f: Double): Vec2d = Vec2d((x * f).toInt, (y * f).toInt)
  def /(n: Int): Vec2d = Vec2d(x / n, y / n)
  def /(f: Double): Vec2d = Vec2d((x / f).toInt, (y / f).toInt)

  def matrixUntil(until: Vec2d): List[Vec2d] = Vec2d
    .matrix(copy(), until - Vec2d(1, 1))
}

object Vec2d {
  def zero: Vec2d = Vec2d(0, 0)

  def matrix(from: Vec2d, to: Vec2d): List[Vec2d] = (from.x to to.x)
    .flatMap(x => (from.y to to.y).map(y => Vec2d(x, y)))
    .toList
}
