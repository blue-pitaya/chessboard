package example.components

import chessboardcore.Vec2d

object Logic {
  def tileSize(boardSize: Vec2d, canvasSize: Vec2d): Int = {
    val maxSize = 100
    val x = canvasSize.x / boardSize.x
    val y = canvasSize.y / boardSize.y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }
}
