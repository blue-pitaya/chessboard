package example

import xyz.bluepitaya.common.Vec2d
import example.models.Piece
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._
import example.game.GameLogic
import example.game.PossibleMoves

trait DrawingObj {
  def id: String
  def position: Vec2d
  def size: Vec2d
}

trait Draggable extends DrawingObj {
  def draggingPosition: Vec2d
}

// some properties will be lost when creating UiState from InternalState
// like list order and ids
case class UiState(tileObjs: List[TileObj], pieceObjs: List[PieceObj])

case class InternalState(tiles: Map[Vec2d, Tile], pieces: Map[Vec2d, Piece])

//TODO: lenses and/or compoanion object of UiState :)
object Mutator {
  private def getDrawingObj(state: UiState, id: String): Option[DrawingObj] = {
    lazy val tileObjOpt = state.tileObjs.find(_.id == id)
    lazy val pieceObjOpt = state.pieceObjs.find(_.id == id)

    tileObjOpt orElse pieceObjOpt
  }

  private def putDrawingObj(state: UiState, obj: DrawingObj): UiState =
    obj match {
      case x: PieceObj => state.copy(pieceObjs =
          state
            .pieceObjs
            .map { p =>
              if (p.id == x.id) x
              else p
            }
        )
      case x: TileObj => state.copy(tileObjs =
          state
            .tileObjs
            .map { t =>
              if (t.id == x.id) x
              else t
            }
        )
    }

  def updateDrawingObj(state: UiState, updatedObj: DrawingObj): UiState = {
    (
      for {
        drawingObj <- getDrawingObj(state, updatedObj.id)
        updatedState = putDrawingObj(state, updatedObj)
      } yield (updatedState)
    ).getOrElse(state)
  }

  // TODO: move piece
  def updatePiece(
      gameState: GameState,
      pieceObj: PieceObj,
      newPosition: Vec2d
  ): GameState = {
    val oldPosition = pieceObj.gamePosition
    (
      for {
        piece <- gameState.pieces.get(oldPosition)
        nextPieces = gameState
          .pieces
          .removed(oldPosition)
          .updated(newPosition, piece)
      } yield (gameState.copy(pieces = nextPieces))
    ).getOrElse(gameState)
  }

  private def moveToBackList[T <: DrawingObj](
      list: List[T],
      obj: T
  ): Option[List[T]] = for {
    x <- list.find(o => o.id == obj.id)
    rest = list.filter(o => o.id != obj.id)
  } yield (rest :+ x)

  // TODO: better name
  def moveToBack(state: UiState, obj: DrawingObj): UiState = {
    obj match {
      case x: PieceObj => state.copy(pieceObjs =
          moveToBackList(state.pieceObjs, x).getOrElse(state.pieceObjs)
        )
      case _ => state
    }
  }

  def highlightTiles(state: UiState, positions: Set[Vec2d]): UiState = state
    .copy(tileObjs =
      state
        .tileObjs
        .map(t =>
          if (positions.contains(t.gamePosition)) t.copy(isMarked = true)
          else t
        )
    )

  def unhighlightTiles(state: UiState): UiState = state
    .copy(tileObjs = state.tileObjs.map(_.copy(isMarked = false)))
}

object Reducer {
  sealed trait Action
  final case class OnStartDragging(obj: Draggable, pointerPosition: Vec2d)
      extends Action
  final case class UpdateDraggingPosition(obj: Draggable, deltaPosition: Vec2d)
      extends Action
  final case class OnEndDragging(obj: Draggable, pointerPosition: Vec2d)
      extends Action

  def stateReduce(
      uiState: UiState,
      gameState: GameState,
      action: Action
  ): UiState = action match {
    case OnStartDragging(obj, pointerPosition) => obj match {
        case x: PieceObj =>
          val updatedObj = x
            .copy(basePosition = (pointerPosition - (x.size * 0.5)))
          val possibleMoves = PossibleMoves.getMoves(x.gamePosition, gameState)

          // TODO: function composition
          val nextState = Mutator.updateDrawingObj(uiState, updatedObj)
          val nextState2 = Mutator.highlightTiles(nextState, possibleMoves)
          Mutator.moveToBack(nextState2, obj)
        case _ =>
          println("error")
          uiState
      }

    case UpdateDraggingPosition(obj, deltaPosition) => obj match {
        case x: PieceObj =>
          val updatedObj = x.copy(draggingPosition = deltaPosition)
          Mutator.updateDrawingObj(uiState, updatedObj)
        case _ =>
          println("error")
          uiState
      }

    case OnEndDragging(obj, pointerPosition) => obj match {
        case x: PieceObj =>
          val newPosition = Renderer
            .toLogicPostion(pointerPosition, Settings.boardDimens)
          val nextGameState = GameLogic
            .makeMove(x.gamePosition, newPosition, gameState)
            .getOrElse(gameState)
          val nextUiState = GlobalState.updateGameState(nextGameState)
          Mutator.unhighlightTiles(nextUiState)

        case _ =>
          println("error!")
          uiState
      }
  }
}
