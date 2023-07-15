package example

import chessboardcore.Vec2d
import dev.bluepitaya.laminardragging.Dragging
import org.http4s.implicits._

object AppModel {
  val DefaultBoardCanvasSize = Vec2d(800, 800)
  // TODO: remove from here
  val ApiPath = uri"http://localhost:8080/"

  import chessboardcore.Model._

  sealed trait DraggingId
  object DraggingId {
    case class PickerPiece(piece: Piece) extends DraggingId
    case class PlacedPiece(fromPos: Vec2d) extends DraggingId
  }

  type DM = Dragging.DraggingModule[DraggingId]

  case class AppState(dm: DM)
  object AppState {
    def init = AppState(dm = Dragging.createModule[DraggingId]())
  }
}
