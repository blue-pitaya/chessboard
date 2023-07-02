package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Vec2f

object DraggingPieceContainer {
  def componentSignal(state: State): Signal[Node] = {
    state
      .draggingPieceState
      .signal
      .map {
        case Some(dpState) =>
          val _centerPos = centerPos(
            dpState.position,
            PiecePicker.pieceImgWidthInPx,
            PiecePicker.pieceImgHeightInPx
          )
          div(
            position.fixed,
            left(toPx(_centerPos.x)),
            top(toPx(_centerPos.y)),
            PiecePicker.pieceImgElement(dpState.piece)
          )
        case None => emptyNode
      }
  }

  private def centerPos(v: Vec2f, w: Double, h: Double): Vec2f =
    Vec2f(v.x - (w / 2), v.y - (h / 2))

  private def toPx(v: Double): String = s"${v.toInt.toString()}px"

  // TODO: move it to better place
  def pieceImgPath(p: Models.Piece): String = PiecePicker.pieceImgPath(p)
}
