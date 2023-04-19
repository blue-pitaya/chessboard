package example

import example.models.HexColor
import xyz.bluepitaya.common.Vec2d
import example.BoardDimens

//FIXME: move to closer context
object Settings {
  // FIXME: get it from gameState
  private val boardSize = Vec2d(8, 8)
  private val boardSizeInPx = Vec2d(800, 800)

  val boardDimens: BoardDimens =
    BoardDimens(logicSize = boardSize, realSizeInPx = boardSizeInPx)

  val tileColorset =
    TileColorset(dark = HexColor("#b58863"), light = HexColor("#f0d9b5"))
}
