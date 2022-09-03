package example

import example.models.Vec2d
import example.models.HexColor
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import example.models.Piece

case class Tile(color: HexColor)

case class TileColorset(dark: HexColor, light: HexColor)

case class Board(tiles: Map[Vec2d, Tile])

case class BoardDimens(logicSize: Vec2d, realSizeInPx: Vec2d)

@JSExportAll
case class TileObj(
    position: Vec2d,
    size: Vec2d,
    color: String,
    fileMark: js.UndefOr[String],
    rankMark: js.UndefOr[String]
)

@JSExportAll
case class PieceObj(
    position: Vec2d,
    size: Vec2d,
    pieceColor: String,
    pieceKind: String
)

object Renderer {
  private def toRealPos(logicPos: Vec2d, boardDimens: BoardDimens): Vec2d = {
    val tileSize = size(boardDimens)
    val xInPx = logicPos.x * tileSize.x
    // note: board positions and render positions has opposite y axis
    val yInPx = boardDimens.realSizeInPx.y - tileSize.y -
      (logicPos.y * tileSize.y)

    Vec2d(xInPx, yInPx)
  }

  private def size(boardDimens: BoardDimens): Vec2d = Vec2d(
    boardDimens.realSizeInPx.x / boardDimens.logicSize.x,
    boardDimens.realSizeInPx.y / boardDimens.logicSize.y
  )

  def getTiles(size: Vec2d, colorset: TileColorset): Board = {
    val positions = Vec2d.matrixUntil(Vec2d.zero, size)
    val tiles = positions
      .map { p =>
        val isDark = (p.x + p.y) % 2 == 0
        val tile = Tile(color = if (isDark) colorset.dark else colorset.light)
        (p -> tile)
      }
      .toMap

    Board(tiles = tiles)
  }

  def renderBoard(board: Board, boardDimens: BoardDimens): Set[TileObj] = {
    def fileMark(position: Vec2d): Option[String] = position match {
      case Vec2d(x, 0) => Some(('a' + x).toChar.toString())
      case _           => None
    }

    def rankMark(position: Vec2d): Option[String] = position match {
      case Vec2d(0, y) => Some((1 + y).toString())
      case _           => None
    }

    board
      .tiles
      .map { case (pos, tile) =>
        TileObj(
          position = toRealPos(pos, boardDimens),
          size = size(boardDimens),
          color = tile.color.value,
          fileMark = fileMark(pos).orUndefined,
          rankMark = rankMark(pos).orUndefined
        )
      }
      .toSet
  }

  def renderPieces(
      pieces: Map[Vec2d, Piece],
      boardDimens: BoardDimens
  ): Set[PieceObj] = {
    pieces
      .map { case (pos, piece) =>
        PieceObj(
          position = toRealPos(pos, boardDimens),
          size = size(boardDimens),
          pieceColor = piece.color.toString(),
          pieceKind = piece.kind.toString()
        )
      }
      .toSet
  }
}
