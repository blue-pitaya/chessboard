package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Vec2f
import example.pages.creator.logic.BoardUiLogic

object DraggingPieceContainer {
  def componentSignal(state: BoardUiLogic.State): Signal[Node] = {
    state
      .draggingPieceState
      .signal
      .withCurrentValueOf(state.boardSize)
      .map { case (dpStateOpt, boardSize) =>
        dpStateOpt match {
          case Some(dpState) =>
            val size = BoardUiLogic.getTileSize(boardSize, state.canvasSize)
            val _centerPos = centerPos(dpState.position, size)
            div(
              position.fixed,
              left(toPx(_centerPos.x)),
              top(toPx(_centerPos.y)),
              PiecePicker.pieceImgElement(dpState.piece, size)
            )
          case None => emptyNode
        }
      }
  }

  private def centerPos(v: Vec2f, size: Int): Vec2f =
    Vec2f(v.x - (size / 2), v.y - (size / 2))

  private def toPx(v: Double): String = s"${v.toInt.toString()}px"

  def pieceImgPath(p: Models.Piece): String = BoardUiLogic.pieceImgPath(p)
}
