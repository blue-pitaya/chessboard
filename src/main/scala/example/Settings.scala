package example

import example.models.HexColor
import example.models.Vec2d

object Settings {
  val tileColorset =
    TileColorset(dark = HexColor("#b58863"), light = HexColor("#f0d9b5"))
  val boardSize = Vec2d(8, 8)
  val boardSizeInPx = Vec2d(800, 800)
}
