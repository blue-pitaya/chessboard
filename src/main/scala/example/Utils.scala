package example

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
}
