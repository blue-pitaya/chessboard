package example

import example.models.Vec2d
import example.models.HexColor
import scala.scalajs.js.annotation._

case class Tile(color: HexColor)

case class TileColorset(dark: HexColor, light: HexColor)

case class Board(tiles: Map[Vec2d, Tile], size: Vec2d)

@JSExportAll
case class TileObj(position: Vec2d, size: Vec2d, color: String)

object Renderer {
  def getTiles(size: Vec2d, colorset: TileColorset): Board = {
    val positions = Vec2d.matrixUntil(Vec2d.zero, size)
    val tiles = positions
      .map { p =>
        val isDark = (p.x + p.y) % 2 == 0
        val tile = Tile(color = if (isDark) colorset.dark else colorset.light)
        (p -> tile)
      }
      .toMap

    Board(tiles = tiles, size = size)
  }

  def renderBoard(totalSizeInPx: Vec2d, board: Board): Set[TileObj] = {
    val tileSize =
      Vec2d(totalSizeInPx.x / board.size.x, totalSizeInPx.y / board.size.y)

    board
      .tiles
      .map { case (pos, tile) =>
        val xInPx = pos.x * tileSize.x
        // note: board positions and render positions has opposite y axis
        val yInPx = totalSizeInPx.y - tileSize.y - (pos.y * tileSize.y)
        val posInPx = Vec2d(xInPx, yInPx)

        TileObj(position = posInPx, size = tileSize, color = tile.color.value)
      }
      .toSet
  }

}
