package example.components

import chessboardcore.Vec2d

object Logic {
  def tileSize(boardSize: Vec2d, canvasSize: Vec2d): Int = {
    val maxSize = 100
    val x = canvasSize.x / boardSize.x
    val y = canvasSize.y / boardSize.y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }

  def boardOffset(tileSize: Int, boardSize: Vec2d, canvasSize: Vec2d): Vec2d =
    (canvasSize - (boardSize * tileSize)) / 2

  def tileCanvasPos(canvasSize: Vec2d, boardSize: Vec2d, pos: Vec2d): Vec2d = {
    val _tileSize = Logic.tileSize(boardSize, canvasSize)
    val _boardOffset = boardOffset(_tileSize, boardSize, canvasSize)
    val x = pos.x * _tileSize
    val y = (canvasSize.y - _tileSize) - (pos.y * _tileSize)

    Vec2d(_boardOffset.x + x, y - _boardOffset.y)
  }
}
