package example.pages.creator

import com.raquo.airstream.state.Var
import dev.bluepitaya.laminardragging.Dragging
import org.scalajs.dom
import chessboardcore.Vec2d
import chessboardcore.Model._
import example.components.DraggingPiece.DraggingPieceState
import example.components.BoardComponent

object CreatorPageModel {
  case class State(
      draggingPieceState: Var[Option[DraggingPieceState]],
      boardContainerRef: Var[Option[dom.Element]],
      boardSize: Var[Vec2d],
      placedPieces: Var[Map[Vec2d, BoardComponent.PieceUiModel]],
      removeZoneComponentRef: Var[Option[dom.Element]]
  )

  sealed trait Event
  case class PickerPieceDragging(e: Dragging.Event, piece: Piece) extends Event
  case class BoardContainerRefChanged(v: dom.Element) extends Event
  case class BoardWidthChanged(v: Int) extends Event
  case class BoardHeightChanged(v: Int) extends Event
  case class PlacedPieceDragging(e: Dragging.Event, fromPos: Vec2d)
      extends Event
  case class RemoveZoneRefChanged(v: dom.Element) extends Event
  case class CreateGameUsingCurrentBoardRequested() extends Event
}
