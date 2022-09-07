package example

import example.models.Vec2d

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._

@JSExportAll
case class JsRenderedState(tiles: js.Array[TileObj], pieces: js.Array[PieceObj])

@js.native
trait JsVec2d extends js.Object {
  def x: Int = js.native
  def y: Int = js.native
}

object Api {
  import Settings._
  import GlobalState._

  private def toVec2d(v: JsVec2d): Vec2d = Vec2d(v.x, v.y)

  private def jsState: JsRenderedState = JsRenderedState(
    tiles = uiState.tileObjs.toJSArray,
    pieces = uiState.pieceObjs.toJSArray
  )

  @JSExportTopLevel("getState")
  def getState(): JsRenderedState = jsState

  @JSExportTopLevel("updateDraggingPosition")
  def updateDraggingPosition(
      obj: Draggable,
      deltaPosition: JsVec2d
  ): JsRenderedState = {
    val action = Reducer.UpdateDraggingPosition(obj, toVec2d(deltaPosition))
    uiState = Reducer.stateReduce(uiState, gameState, action)

    getState()
  }

  @JSExportTopLevel("onEndDragging")
  def onEndDragging(
      obj: Draggable,
      pointerPosition: JsVec2d
  ): JsRenderedState = {
    val action = Reducer.OnEndDragging(obj, toVec2d(pointerPosition))
    uiState = Reducer.stateReduce(uiState, gameState, action)

    getState()
  }

  @JSExportTopLevel("onStartDragging")
  def onStartDragging(obj: Draggable): JsRenderedState = {
    val action = Reducer.OnStartDragging(obj)
    uiState = Reducer.stateReduce(uiState, gameState, action)

    getState()
  }
}
