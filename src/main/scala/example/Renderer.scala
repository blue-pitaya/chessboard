package example

import xyz.bluepitaya.common.Vec2d
import example.models.HexColor
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import example.models.Piece

case class Tile(color: HexColor, isMarked: Boolean = false)

case class TileColorset(dark: HexColor, light: HexColor)

case class BoardDimens(logicSize: Vec2d, realSizeInPx: Vec2d)

object Renderer {
  def getTileSize(boardDimens: BoardDimens): Vec2d = Vec2d(
    boardDimens.realSizeInPx.x / boardDimens.logicSize.x,
    boardDimens.realSizeInPx.y / boardDimens.logicSize.y
  )

  def toRealPos(logicPos: Vec2d, boardDimens: BoardDimens): Vec2d = {
    val tileSize = getTileSize(boardDimens)
    val xInPx = logicPos.x * tileSize.x
    // note: board positions and render positions has opposite y axis
    val yInPx = boardDimens.realSizeInPx.y - tileSize.y -
      (logicPos.y * tileSize.y)

    Vec2d(xInPx, yInPx)
  }

  def getTiles(
      size: Vec2d,
      colorset: TileColorset,
      markedPoses: Set[Vec2d] = Set()
  ): Map[Vec2d, Tile] = Vec2d
    .zero
    .matrixUntil(size)
    .map { p =>
      val isDark = (p.x + p.y) % 2 == 0
      val tile = Tile(
        color =
          if (isDark) colorset.dark
          else colorset.light,
        isMarked = markedPoses.contains(p)
      )
      (p -> tile)
    }
    .toMap

  def toLogicPostion(pos: Vec2d, boardDimens: BoardDimens): Vec2d = Vec2d(
    pos.x / getTileSize(boardDimens).x,
    (boardDimens.realSizeInPx.y - pos.y) / getTileSize(boardDimens).y
  )
}
