package example.pages.creator

import com.raquo.airstream.state.Var
import dev.bluepitaya.laminardragging.Dragging
import org.scalajs.dom
import chessboardcore.Vec2d
import chessboardcore.Model._
import cats.effect.IO

object ExAppModel {
  val DefaultBoardCanvasSize = Vec2d(800, 800)

  sealed trait PieceDraggingId
  case class PickerPieceDraggingId(kind: PieceKind, color: PieceColor)
      extends PieceDraggingId
  case class PlacedPieceDraggingId(fromPos: Vec2d) extends PieceDraggingId

  case class DraggingPieceState(imgPath: String, draggingEvent: Dragging.Event)

  case class PieceOnBoard(piece: Piece, isVisible: Var[Boolean])

  type PlacedPieces = Map[Vec2d, PieceOnBoard]
  type EvHandler = Ev => IO[Unit]

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
      canvasSize = DefaultBoardCanvasSize,
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
