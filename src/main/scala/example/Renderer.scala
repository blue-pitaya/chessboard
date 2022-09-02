package example

import java.awt.Color
import example.models.Vec2d
import scala.scalajs.js._
import scala.scalajs.js.annotation._

case class Tile(color: Color)

case class TileColorset(dark: Color, light: Color)

object Renderer {
  @JSExportTopLevel("renderTiles")
  def renderTiles(size: Vec2d, colorset: TileColorset): Map[Vec2d, Tile] = ???
}
