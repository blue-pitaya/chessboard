package example

import example.models.Vec2d
import example.models.Vec2d._
import example.models.Piece
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._

trait DrawingObj {
  def id: String
  def position: Vec2d
  def size: Vec2d
}

trait Draggable extends DrawingObj {
  def draggingPosition: Vec2d
}

case class RenderedState(tileObjs: Set[TileObj], pieceObjs: Set[PieceObj])

case class InternalState(tiles: Map[Vec2d, Tile], pieces: Map[Vec2d, Piece])

//TODO: lenses?
object Mutator {
  private def getDrawingObj(
      state: RenderedState,
      id: String
  ): Option[DrawingObj] = {
    lazy val tileObjOpt = state.tileObjs.find(_.id == id)
    lazy val pieceObjOpt = state.pieceObjs.find(_.id == id)

    tileObjOpt orElse pieceObjOpt
  }

  private def putDrawingObj(
      state: RenderedState,
      obj: DrawingObj
  ): RenderedState = obj match {
    case x: PieceObj => state.copy(pieceObjs =
        state.pieceObjs.map { p => if (p.id == x.id) x else p }
      )
    case x: TileObj => state
        .copy(tileObjs = state.tileObjs.map { t => if (t.id == x.id) x else t })
  }

  def updateDrawingObj(
      state: RenderedState,
      updatedObj: DrawingObj
  ): RenderedState = {
    (for {
      drawingObj <- getDrawingObj(state, updatedObj.id)
      updatedState = putDrawingObj(state, updatedObj)
    } yield (updatedState)).getOrElse(state)
  }
}

object Reducer {
  sealed trait Action
  final case class UpdateDraggingPosition(obj: Draggable, deltaPosition: Vec2d)
      extends Action
  final case class OnEndDragging(obj: Draggable) extends Action

  def stateReduce(state: RenderedState, action: Action): RenderedState =
    action match {
      case UpdateDraggingPosition(obj, deltaPosition) =>
        val updatedObj = obj match {
          case x: PieceObj => x.copy(draggingPosition = deltaPosition)
        }
        Mutator.updateDrawingObj(state, updatedObj)

      case OnEndDragging(obj) =>
        val updatedObj = obj match {
          case x: PieceObj =>
            x.copy(basePosition = x.position, draggingPosition = Vec2d.zero)
        }
        Mutator.updateDrawingObj(state, updatedObj)
    }
}
