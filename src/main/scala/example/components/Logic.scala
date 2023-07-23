package example.components

import chessboardcore.Vec2d
import dev.bluepitaya.laminardragging.Vec2f

object Logic {
  def tileSize(boardSize: Vec2d, canvasSize: Vec2d): Int = {
    val maxSize = 100
    val x = canvasSize.x / boardSize.x
    val y = canvasSize.y / boardSize.y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }

  def boardOffset(tileSize: Int, boardSize: Vec2d, canvasSize: Vec2d): Vec2d =
    (canvasSize - (boardSize * tileSize)) / 2

  def tileCanvasPos(
      canvasSize: Vec2d,
      boardSize: Vec2d,
      pos: Vec2d,
      boardFlipped: Boolean
  ): Vec2d = {
    val _tileSize = tileSize(boardSize, canvasSize)
    val _boardOffset = boardOffset(_tileSize, boardSize, canvasSize)
    val x =
      if (boardFlipped)
        ((canvasSize.x - _tileSize) - (pos.x * _tileSize) - _boardOffset.x)
      else _boardOffset.x + (pos.x * _tileSize)
    val y =
      if (boardFlipped) _boardOffset.y + (pos.y * _tileSize)
      else ((canvasSize.y - _tileSize) - (pos.y * _tileSize) - _boardOffset.y)

    Vec2d(x, y)
  }

  def isPosOnBoard(pos: Vec2d, boardSize: Vec2d): Boolean =
    isBetween(pos, Vec2d.zero, boardSize)

  private def isBetween(v: Vec2d, b1: Vec2d, b2: Vec2d): Boolean =
    v.x >= b1.x && v.y >= b1.y && v.x < b2.x && v.y < b2.y

  def tileLogicPos(
      boardSize: Vec2d,
      canvasSize: Vec2d,
      canvasPos: Vec2d,
      boardFlipped: Boolean
  ): Option[Vec2d] = {
    val _tileSize = tileSize(boardSize, canvasSize)
    val _boardOffset = boardOffset(_tileSize, boardSize, canvasSize)
    val onBoardPos = (canvasPos - _boardOffset)
    val pos = Vec2f(
      onBoardPos.x.toDouble / _tileSize,
      onBoardPos.y.toDouble / _tileSize
    )
    lazy val vec2dPos = toVec2dRoundedDown(pos)
    lazy val finalPos =
      if (boardFlipped) invertXAxis(vec2dPos, boardSize.x)
      else invertYAxis(vec2dPos, boardSize.y)

    Option.when(isBetween(pos, Vec2f.zero, toVec2f(boardSize)))(finalPos)
  }

  private def isBetween(v: Vec2f, b1: Vec2f, b2: Vec2f): Boolean =
    v.x >= b1.x && v.y >= b1.y && v.x < b2.x && v.y < b2.y

  private def toVec2f(v: Vec2d): Vec2f = Vec2f(v.x, v.y)

  private def invertYAxis(v: Vec2d, h: Int): Vec2d = Vec2d(v.x, h - v.y - 1)

  private def invertXAxis(v: Vec2d, w: Int): Vec2d = Vec2d(w - v.x - 1, v.y)

  private def toVec2dRoundedDown(v: Vec2f): Vec2d = Vec2d(v.x.toInt, v.y.toInt)
}
