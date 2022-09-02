package example

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._
import scala.scalajs.js.annotation._
import example.models.Vec2d

object Api {
  import Settings._

  @JSExportTopLevel("renderTiles")
  def renderTiles(): js.Array[TileObj] = Renderer
    .renderBoard(boardSizeInPx, Renderer.getTiles(boardSize, tileColorset))
    .toJSArray
}
