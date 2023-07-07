package example.exp

import com.raquo.airstream.state.Var
import dev.bluepitaya.laminardragging.Dragging
import org.scalajs.dom
import example.game.Vec2d

object ExAppModel {
  sealed trait Fig
  case object Pawn extends Fig
  case object Rook extends Fig
  case object Knight extends Fig
  case object Bishop extends Fig
  case object Queen extends Fig
  case object King extends Fig

  sealed trait FigColor
  case object White extends FigColor
  case object Black extends FigColor

  case class PieceDraggingId(piece: Fig, color: FigColor)

  case class DraggingPieceState(imgPath: String, position: Vec2d)

  case class State(
      dm: Dragging.DraggingModule[PieceDraggingId],
      draggingPieceState: Var[Option[DraggingPieceState]],
      containerRef: Var[Option[dom.Element]],
      boardSize: Var[Vec2d]
  )
  object State {
    def init = State(
      dm = Dragging.createModule[ExAppModel.PieceDraggingId](),
      draggingPieceState = Var(None),
      containerRef = Var(None),
      boardSize = Var(Vec2d(6, 6))
    )
  }

  sealed trait Ev
  case class PickerPieceDragging(e: Dragging.Event, piece: Fig, color: FigColor)
      extends Ev
  case class BoardContainerRefChanged(v: dom.Element) extends Ev
  case class BoardWidthChanged(v: Int) extends Ev
  case class BoardHeightChanged(v: Int) extends Ev
}
