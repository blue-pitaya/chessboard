package example.pages.creator

import com.raquo.airstream.state.Var
import dev.bluepitaya.laminardragging.Dragging
import org.scalajs.dom
import chessboardcore.Vec2d
import chessboardcore.Model._

object ExAppModel {
  sealed trait PieceDraggingId
  case class PickerPieceDraggingId(piece: Fig, color: FigColor)
      extends PieceDraggingId
  case class PlacedPieceDraggingId(fromPos: Vec2d) extends PieceDraggingId

  case class DraggingPieceState(imgPath: String, draggingEvent: Dragging.Event)

  case class ColoredPiece(color: FigColor, piece: Fig, isVisible: Var[Boolean])

  type PlacedPieces = Map[Vec2d, ColoredPiece]

  case class State(
      dm: Dragging.DraggingModule[PieceDraggingId],
      draggingPieceState: Var[Option[DraggingPieceState]],
      boardContainerRef: Var[Option[dom.Element]],
      boardSize: Var[Vec2d],
      canvasSize: Vec2d,
      placedPieces: Var[PlacedPieces],
      removeZoneComponentRef: Var[Option[dom.Element]]
  )
  object State {
    def init = State(
      dm = Dragging.createModule[ExAppModel.PieceDraggingId](),
      draggingPieceState = Var(None),
      boardContainerRef = Var(None),
      boardSize = Var(Vec2d(6, 6)),
      canvasSize = Vec2d(800, 800),
      placedPieces = Var(Map()),
      removeZoneComponentRef = Var(None)
    )
  }

  sealed trait Ev
  case class PickerPieceDragging(e: Dragging.Event, piece: Fig, color: FigColor)
      extends Ev
  case class BoardContainerRefChanged(v: dom.Element) extends Ev
  case class BoardWidthChanged(v: Int) extends Ev
  case class BoardHeightChanged(v: Int) extends Ev
  case class PlacedPieceDragging(e: Dragging.Event, fromPos: Vec2d) extends Ev
  case class RemoveZoneRefChanged(v: dom.Element) extends Ev
  case class SaveBoardRequested() extends Ev
}
