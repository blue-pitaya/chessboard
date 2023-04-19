package example

import xyz.bluepitaya.common.Vec2d
import example.models.Piece
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._
import example.game.GameLogic
import example.game.GameState
import example.game.PossibleMoves

trait DrawingObj {
  def id: String
  def position: Vec2d
  def size: Vec2d
}

trait Draggable extends DrawingObj {
  def draggingPosition: Vec2d
}

case class InternalState(tiles: Map[Vec2d, Tile], pieces: Map[Vec2d, Piece])

//TODO: lenses and/or compoanion object of UiState :)
object Mutator {

  // TODO: move piece
  // def updatePiece(
  //    gameState: GameState,
  //    pieceObj: PieceObj,
  //    newPosition: Vec2d
  // ): GameState = {
  //  val oldPosition = pieceObj.gamePosition
  //  (
  //    for {
  //      piece <- gameState.pieces.get(oldPosition)
  //      nextPieces = gameState
  //        .pieces
  //        .removed(oldPosition)
  //        .updated(newPosition, piece)
  //    } yield (gameState.copy(pieces = nextPieces))
  //  ).getOrElse(gameState)
  // }

  private def moveToBackList[T <: DrawingObj](
      list: List[T],
      obj: T
  ): Option[List[T]] = for {
    x <- list.find(o => o.id == obj.id)
    rest = list.filter(o => o.id != obj.id)
  } yield (rest :+ x)

  //// TODO: better name
  // def moveToBack(state: UiState, obj: DrawingObj): UiState = {
  //  obj match {
  //    case x: PieceObj => state.copy(pieceObjs =
  //        moveToBackList(state.pieceObjs, x).getOrElse(state.pieceObjs)
  //      )
  //    case _ => state
  //  }
  // }
}

//object Reducer {
//  sealed trait Action
//  final case class OnStartDragging(obj: Draggable, pointerPosition: Vec2d)
//      extends Action
//  final case class UpdateDraggingPosition(obj: Draggable, deltaPosition: Vec2d)
//      extends Action
//  final case class OnEndDragging(obj: Draggable, pointerPosition: Vec2d)
//      extends Action
//
//  def stateReduce(
//      uiState: UiState,
//      gameState: GameState,
//      action: Action
//  ): UiState = action match {
//    case OnStartDragging(obj, pointerPosition) => obj match {
//        case x: PieceObj =>
//          val updatedObj = x
//            .copy(basePosition = (pointerPosition - (x.size * 0.5)))
//          val possibleMoves = PossibleMoves
//            .getMoveTiles(x.gamePosition, gameState)
//            .map(_._1)
//            .toSet
//
//          // TODO: function composition
//          val nextState = Mutator.updateDrawingObj(uiState, updatedObj)
//          val nextState2 = Mutator.highlightTiles(nextState, possibleMoves)
//          Mutator.moveToBack(nextState2, obj)
//        case _ =>
//          println("error")
//          uiState
//      }
//
//    case UpdateDraggingPosition(obj, deltaPosition) => obj match {
//        case x: PieceObj =>
//          val updatedObj = x.copy(draggingPosition = deltaPosition)
//          Mutator.updateDrawingObj(uiState, updatedObj)
//        case _ =>
//          println("error")
//          uiState
//      }
//
//    case OnEndDragging(obj, pointerPosition) => obj match {
//        case x: PieceObj =>
//          val newPosition = Renderer
//            .toLogicPostion(pointerPosition, Settings.boardDimens)
//          val nextGameState = GameLogic
//            .makeMove(x.gamePosition, newPosition, gameState)
//            .getOrElse(gameState)
//          val nextUiState = GlobalState.updateGameState(nextGameState)
//          Mutator.unhighlightTiles(nextUiState)
//
//        case _ =>
//          println("error!")
//          uiState
//      }
//  }
//}
