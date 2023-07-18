package example.pages.creator

import com.raquo.airstream.state.Var
import dev.bluepitaya.laminardragging.Dragging
import org.scalajs.dom
import chessboardcore.Vec2d
import chessboardcore.Model._
import example.components.DraggingPiece.DraggingPieceState
import example.components.BoardComponent

object ExAppModel {
  case class State(
      draggingPieceState: Var[Option[DraggingPieceState]],
      boardContainerRef: Var[Option[dom.Element]],
      boardSize: Var[Vec2d],
      placedPieces: Var[Map[Vec2d, BoardComponent.PieceUiModel]],
      removeZoneComponentRef: Var[Option[dom.Element]]
  )
  object State {
    def init = State(
      draggingPieceState = Var(None),
      boardContainerRef = Var(None),
      boardSize = Var(Vec2d(6, 6)),
      placedPieces = Var(Map()),
      removeZoneComponentRef = Var(None)
    )
  }

  sealed trait Ev
  case class PickerPieceDragging(e: Dragging.Event, piece: Piece) extends Ev
  case class BoardContainerRefChanged(v: dom.Element) extends Ev
  case class BoardWidthChanged(v: Int) extends Ev
  case class BoardHeightChanged(v: Int) extends Ev
  case class PlacedPieceDragging(e: Dragging.Event, fromPos: Vec2d) extends Ev
  case class RemoveZoneRefChanged(v: dom.Element) extends Ev
  case class SaveBoardRequested() extends Ev
  case class CreateGameUsingCurrentBoardRequested() extends Ev
}
