package example.pages.creator

import com.raquo.laminar.api.L._
import example.game.Vec2d
import dev.bluepitaya.laminardragging.Vec2f

case class DraggingPieceState(piece: Models.Piece, position: Vec2f)
case class State(
    boardState: BoardContainer.State,
    draggingPieceState: Var[Option[DraggingPieceState]]
)

object State {
  def init(boardLogicSize: Vec2d) = State(
    boardState = BoardContainer
      .State(boardLogicSize = boardLogicSize, boardRealSize = Vec2d(800, 800)),
    draggingPieceState = Var(None)
  )
}
