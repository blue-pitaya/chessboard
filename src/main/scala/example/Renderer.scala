package example

import example.models.Vec2d
import example.models.Color
import scala.scalajs.js.annotation._

@JSExportAll
case class Tile(color: Color)

@JSExportAll
case class TileColorset(dark: Color, light: Color)

object Renderer {
  @JSExportTopLevel("renderTiles")
  def renderTiles(size: Vec2d, colorset: TileColorset): Map[Vec2d, Tile] = {
    val positions = Vec2d.matrixUntil(Vec2d.zero, size)
    positions
      .map { p =>
        val isDark = (p.x + p.y) % 2 == 0
        val tile = Tile(color = if (isDark) colorset.dark else colorset.light)
        (p -> tile)
      }
      .toMap
  }
}
