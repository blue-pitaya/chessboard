package example.pages.creator

//case class BoardSize(w: Var[Int], h: Var[Int])

case class State(boardState: BoardContainer.State)

object State {
  def init = State(
    // boardSize = BoardSize(w = Var(3), h = Var(3)),
    boardState = BoardContainer.State.empty
  )
}
