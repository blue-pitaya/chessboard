package example.pages.creator

import com.raquo.laminar.api.L._

case class BoardSize(w: Var[Int], h: Var[Int])

case class State(boardSize: BoardSize)

object State {
  def init = State(boardSize = BoardSize(w = Var(3), h = Var(3)))
}
